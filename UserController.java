package com.zeptoh.benchmarking.controllers;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zeptoh.benchmarking.interfaces.ClientService;
import com.zeptoh.benchmarking.interfaces.LicenseService;
import com.zeptoh.benchmarking.interfaces.ProjectService;
import com.zeptoh.benchmarking.interfaces.ReportService;
import com.zeptoh.benchmarking.interfaces.SecurityService;
import com.zeptoh.benchmarking.interfaces.UserService;
import com.zeptoh.benchmarking.interfaces.WellService;
import com.zeptoh.benchmarking.model.Client;
import com.zeptoh.benchmarking.model.ForgetPassword;
import com.zeptoh.benchmarking.model.License;
import com.zeptoh.benchmarking.model.Login;
import com.zeptoh.benchmarking.model.Project;
import com.zeptoh.benchmarking.model.Well;
import com.zeptoh.benchmarking.model.WellConfiguration;
import com.zeptoh.benchmarking.model.WellInputPhase;
import com.zeptoh.benchmarking.model.WellRequest;
import com.zeptoh.benchmarking.repository.ClientRepository;
import com.zeptoh.benchmarking.repository.ForgetPasswordRepository;
import com.zeptoh.benchmarking.repository.LoginRepository;
import com.zeptoh.benchmarking.repository.WellConfigurationRepository;
import com.zeptoh.benchmarking.repository.WellRepository;
import com.zeptoh.benchmarking.repository.WellRequestRepository;
import com.zeptoh.benchmarking.services.UserServiceImpl;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private ForgetPasswordRepository forgetPasswordRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
	WellConfigurationRepository wellConfigurationRepository;

	@Autowired
	WellRequestRepository wellRequestRepository;
	
	@Autowired
	WellRepository wellRepository;
	
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private LicenseService licenseService;
    
    @Autowired
    private WellService wellService;

    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model) { 
       model.addAttribute("userForm", new Login());
        return "register";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@ModelAttribute("userForm") Login userForm, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }
 
        userService.save(userForm);
        Login login=loginRepository.findByUserId(userForm.getUserId());
    	Client client=clientRepository.findByUserId(login.getUserId());
    	if(client!=null) {
    	String message = "IwellsBenchmarking Notice: Registration Notice";
    	String role = "ROLE_SUPER_ADMIN";
    		
    	List<Login> user1 = loginRepository.findByRole(role);
    	Iterator<Login> itr = user1.iterator();
    	while (itr.hasNext()) {
    		Login login1 = itr.next();
    		String adminEmail = login1.getUserId();
    		if (adminEmail != null) {
    	String body ="<h3>Hi SuperAdmin,</h3>" 
    			+"<p>The user"
    			+" "
    	+ login.getFirstName()
    	+" "
    	+ login.getLastName()
    	+" has been registered successfully.</p>";
    	//String reportId=login.getReportId();
    	UserServiceImpl userService = new UserServiceImpl();
    	userService.singleEmail(body, adminEmail, message);
    	String adminbody ="<h3>Hi"
    			+" "
    	+login.getFirstName()
    	+" "
    	+login.getLastName()
    	+",</h3>"
    	+" "
    	+" <p>your account has been registered with iwells successfully.</p>";
    	String adminId=login.getUserId();
    	UserServiceImpl euserService = new UserServiceImpl();
    	euserService.singleEmail(adminbody, adminId, message);
    	model.addAttribute("mail", "send mail successfully to superadmin and admin");
    		}
    		else {
    	    	model.addAttribute("mail", "user not found");

    		}
    	}
    	}
        //securityService.autologin(userForm.getUserId(), userForm.getPassword());

        return "redirect:/dashboard";
    }
    
    @RequestMapping(value = "/clientRegistration", method = RequestMethod.POST)
    @ResponseBody
    public String clientRegistration(@ModelAttribute("clientForm") Client clientForm, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        clientService.save(clientForm);
 
        return "Client Registered Successfully";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "redirect:/dashboard";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@ModelAttribute("userForm") Login userForm, Model model,
    		HttpServletRequest request, 
            HttpServletResponse response) {
    	boolean hasError = false;
    	
    	Login login = userService.findByUserId(userForm.getUserId());
    	Client client = null;
    	if(login!=null) client = clientService.findById(login.getClientId());
    	
        if((login!=null) && (login.getRole().equals("ROLE_SUPER_ADMIN") || (client != null && client.getStatus().equals("approved")))){
        	
        	License license = null;
        			
        	if(!login.getRole().equals("ROLE_SUPER_ADMIN")) {
	        	license = licenseService.findByClientId(client.getId());
	        	
	        	List<Login> usersList = loginRepository.findByClientId(client.getId());
	        	int loggedInUsers = 0;
	        	Iterator<Login> itr = usersList.iterator();
	        	while(itr.hasNext()){
	        		Login lUser = itr.next();
	        		if(lUser.isLoggedIn()) loggedInUsers++;
	        	}
	        	
	        	if(license !=null && loggedInUsers == license.getUserLicenses().getNoOfUsers()){
	        		model.addAttribute("showError", true);
		            model.addAttribute("errorMessage", "Reached the maximum limit of users login at a time. \n Already " + license.getUserLicenses().getNoOfUsers() + " users are logged in.");
		            return "index";
	        	}
        	}
	    	
        	try{
	    		securityService.autologin(userForm.getUserId(), userForm.getPassword());
	    	} catch(Exception e){
	    		e.printStackTrace();
	    		hasError = true;
	    	}
	    	
	        if (hasError){
	        	model.addAttribute("showError", true);
	            model.addAttribute("errorMessage", "Either your username or password is not valid.");
	            return "index";
	        }
	        else {
	        	login.setLoggedIn(true);
	    		loginRepository.save(login);
	    		
	        	HttpSession session = request.getSession();
	            session.setAttribute("user", login);
	            model.addAttribute("userId", login.getId());
	            model.addAttribute("clientId", login.getClientId());
	        	model.addAttribute("userName", login.getFirstName()+" "+login.getLastName());
	        	model.addAttribute("userEmail", login.getUserId());
	        	model.addAttribute("userRole", login.getRole());
	        	
	        	if(!login.getRole().equals("ROLE_SUPER_ADMIN")) {        	
		        	if(license == null){
		        		model.addAttribute("userNotify", "Your License is not set");
		        	}
	        	}
	        	
	        	String role = login.getRole();
	            return getDashboardPage(role);
	        }
        } else {
        	if(client != null && client.getStatus().equals("pending")){
	        	model.addAttribute("showError", true);
	            model.addAttribute("errorMessage", "Your Account is not Activated Yet. Please contact support Team.");
	            return "index";
        	} else {
        		model.addAttribute("showError", true);
	            model.addAttribute("errorMessage", "Account does not exist.");
	            return "index";
        	}
        }
    }
     
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String login(Model model,
    		HttpServletRequest request, 
            HttpServletResponse response) {
    	HttpSession session = request.getSession();
    	if(session != null && session.getAttribute("user") != null){
	    	Login user = (Login) session.getAttribute("user");
	    	model.addAttribute("userId", user.getId());
	    	model.addAttribute("clientId", user.getClientId());
        	model.addAttribute("userName", user.getFirstName()+" "+user.getLastName());
        	model.addAttribute("userEmail", user.getUserId());
	    	model.addAttribute("userRole", user.getRole());
	        return "redirect:/dashboard";
    	} else {
    		return "index";
    	}
    }
    
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String getProfile(Model model,
    		HttpServletRequest request, 
            HttpServletResponse response) {
    	HttpSession session = request.getSession();
    	Login user = (Login) session.getAttribute("user");
    	model.addAttribute("userId", user.getId());
    	model.addAttribute("clientId", user.getClientId());
    	model.addAttribute("userName", user.getFirstName()+" "+user.getLastName());
    	model.addAttribute("userEmail", user.getUserId());
    	model.addAttribute("userRole", user.getRole());
    	
    	String role = user.getRole();

        return getDashboardPage(role);
    }
    
    public String getDashboardPage(String role){
    	String dPage = "eDashboard";
        
        if(role.equals("ROLE_SUPER_ADMIN")){
        	dPage = "saDashboard";
        } else if(role.equals("ROLE_ADMIN")){
        	dPage = "aDashboard";
        } else if(role.equals("ROLE_WELL_MANAGER")){
        	dPage = "mDashboard";
        } else {
        	dPage = "eDashboard";
        }
        
        return dPage;
    }
	
		@RequestMapping(value = "/about", method = RequestMethod.GET)
	public String aboutSection(Model model) {
		return "about";
	}

	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	public String contactSection(Model model) {
		return "contact";
	}

	@RequestMapping(value = "/resetPassword", produces = "application/json")
	public String resetPassword(@RequestParam("userId") String userId, @RequestParam("resetToken") String resetToken,Model model) {
		ForgetPassword forgetPasswordEntity = forgetPasswordRepository.findByUserIdAndForgetPasswordToken(userId,
				resetToken);
		final Calendar cal = Calendar.getInstance();

		if (forgetPasswordEntity != null) {
			System.out.println(
					"expiration time:  " + (forgetPasswordEntity.getExpiryDate().getTime() - cal.getTime().getTime()));
			if ((forgetPasswordEntity.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
				if (forgetPasswordEntity.isFlag()) {
					return "resetPassword";
				}

				else {
					model.addAttribute("mail", "Reset password has already changed.");
					return "linkExpiration";
									}
			} else {
				model.addAttribute("mail", "Reset password link has been expired");
				return "linkExpiration";
				
			}
		}
		model.addAttribute("mail", "Reset password link has been expired");
		return "linkExpiration";	}


	@RequestMapping(value = "/faq", method = RequestMethod.GET)
	public String frequentlyAskedQuestions(Model model) {
		return "faq";
	}
	@RequestMapping(value = "/wellRequest", method = RequestMethod.GET)
	public String wellRequest(@RequestParam("tokenId") String tokenId,
Model model) {
		WellRequest client=wellRequestRepository.findByTokenId(tokenId);
if(client!=null && client.isFlag()) {
    	return "wellRequest";
}
else {
	model.addAttribute("mail", "link has been expired");
return "linkExpiration";
}
	}
	@RequestMapping(value = "/requestWell", method = RequestMethod.GET)
	public String requestWell(Model model) {
		
    	return "requestWell";
	}
	@RequestMapping(value = "/pdfreport/{level}/{config}/{wellId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> citiesReport(@PathVariable("level") String level,@PathVariable("config") String config,@PathVariable("wellId") String wellId) {
		WellConfiguration wellConfiguration = wellConfigurationRepository.findByWellIdLevelAndConfigNo(wellId,level,config);
		Well well = wellRepository.findById(wellId);
Project project = projectService.findById(well.getProjectId());
String projectName = project.getProjectName();
		ByteArrayInputStream bis = reportService.generatePdfReport(wellConfiguration,well,projectName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=citiesreport.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}