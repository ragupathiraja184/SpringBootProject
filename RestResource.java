package com.zeptoh.benchmarking.resources;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import springfox.documentation.spring.web.json.Json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeptoh.benchmarking.model.ActivityLog;
import com.zeptoh.benchmarking.model.Client;
import com.zeptoh.benchmarking.model.ForgetPassword;
import com.zeptoh.benchmarking.model.License;
import com.zeptoh.benchmarking.model.Login;
import com.zeptoh.benchmarking.model.Project;
import com.zeptoh.benchmarking.model.RealTimeCostModal;
import com.zeptoh.benchmarking.model.RealTimeTotalCost;
import com.zeptoh.benchmarking.model.RealTimeWellOperation;
import com.zeptoh.benchmarking.model.Role;
import com.zeptoh.benchmarking.model.Template;
import com.zeptoh.benchmarking.model.UserLicense;
import com.zeptoh.benchmarking.model.Well;
import com.zeptoh.benchmarking.model.WellConfiguration;
import com.zeptoh.benchmarking.model.WellInputJson;
import com.zeptoh.benchmarking.model.WellRequest;
import com.zeptoh.benchmarking.model.WellUnit;
import com.zeptoh.benchmarking.repository.ClientRepository;
import com.zeptoh.benchmarking.repository.ForgetPasswordRepository;
import com.zeptoh.benchmarking.repository.LicenseRepository;
import com.zeptoh.benchmarking.repository.LoginRepository;
import com.zeptoh.benchmarking.repository.ProjectRepository;
import com.zeptoh.benchmarking.repository.RealTimeCostModalRepository;
import com.zeptoh.benchmarking.repository.RealTimeWellOperationRepository;
import com.zeptoh.benchmarking.repository.RoleRepository;
import com.zeptoh.benchmarking.repository.TemplateRepository;
import com.zeptoh.benchmarking.repository.UserLicenseRepository;
import com.zeptoh.benchmarking.repository.WellConfigurationRepository;
import com.zeptoh.benchmarking.repository.WellRepository;
import com.zeptoh.benchmarking.repository.WellRequestRepository;
import com.zeptoh.benchmarking.repository.WellUnitRepository;
import com.zeptoh.benchmarking.services.UserServiceImpl;
import com.zeptoh.benchmarking.utils.TestUtils;
import com.zeptoh.benchmarking.utils.Utils;

@RestController
@RequestMapping("api")
public class RestResource {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	TemplateRepository templateRepository;
	
	@Autowired
	RealTimeCostModalRepository realTimeCostModalRepository;
	
	@Autowired
	LoginRepository userRepository;

	@Autowired
	ClientRepository clientRepository;

	@Autowired
	LicenseRepository licenseRepository;

	@Autowired
	WellRequestRepository wellRequestRepository;

	@Autowired
	UserLicenseRepository userLicenseRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	WellRepository wellRepository;

	@Autowired
	WellUnitRepository UnitRepository;

	@Autowired
	ForgetPasswordRepository forgetPasswordRepository;
	
	@Autowired
	WellConfigurationRepository wellConfigurationRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private RealTimeWellOperationRepository realTimeWellOperationRepository;
	private ObjectMapper mapper = new ObjectMapper();

	private String id;

	@RequestMapping(value = "/addClient", method = RequestMethod.POST, produces = "application/json")
	public String addClient(@RequestBody Client client) {
		try {
			clientRepository.save(client);
			return "Success:" + client.getId();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/addLicense", method = RequestMethod.GET, produces = "application/json")
	public String addLicense(@RequestParam("clientId") String clientId) {
		try {
			License license = new License();
			Client client = clientRepository.findById(clientId);
			license.setClientId(clientId);
			license.setCreatedDate(new Date());
			license.setLastUpdatedDate(new Date());
			license.setStartDate(new Date());
			license.setUserLicenses(userLicenseRepository.findByNoOfUsers(1));
			license.setFirst(true);
			license.setActivityLog(new ArrayList<ActivityLog>());
			if (client.getLicenseType().equalsIgnoreCase("custom")) {
				license.addActivityLog(Utils.setLogs("Request License: No of Wells-" + client.getNoOfWells()
						+ "; No of Concurrent Users-" + client.getNoOfUsers() + "; No of Well Manager-"
						+ client.getNoOfWellManager() + "; No of Well Engineer-" + client.getNoOfWellEngineer()
						+ "; No of Other User-" + client.getNoOfOtherUser()));
			} else {
				license.addActivityLog(Utils.setLogs("Request License: No of Wells-" + client.getNoOfWells()
						+ "; No of Concurrent Users-" + client.getNoOfUsers()));
			}

			license.addActivityLog(Utils.setLogs(
					"First License created: No of Wells-" + license.getNoOfWells() + "; No of Concurrent Users-1"));
			licenseRepository.save(license);
			return "Success:" + license.getId();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/addUser", method = RequestMethod.POST, produces = "application/json")
	public String addUser(@RequestBody Login login) {
		try {
			login.setPassword(bCryptPasswordEncoder.encode(login.getPassword()));
			String role = login.getRole();

			License license = licenseRepository.findByClientId(login.getClientId());

			if (license != null) {
				List<Login> usersList = userRepository.findByClientId(login.getClientId());
				int noOfWellManagers = 0;
				int noOfWellEngineers = 0;
				int noOfOtherUsers = 0;

				Iterator<Login> itr = usersList.iterator();
				while (itr.hasNext()) {
					Login lUser = itr.next();
					if (lUser.getRole().equals("ROLE_WELL_MANAGER"))
						noOfWellManagers++;
					if (lUser.getRole().equals("ROLE_WELL_ENGINEER"))
						noOfWellEngineers++;
					if (lUser.getRole().equals("ROLE_OTHER_USER"))
						noOfOtherUsers++;
				}
				switch (role) {
				case "ROLE_WELL_MANAGER":
					if (noOfWellManagers >= license.getUserLicenses().getNoOfWellManager()) {
						return "Error:Reached Maximum limit of Well Managers. Please upgrade License.";
					}
				case "ROLE_WELL_ENGINEER":
					if (noOfWellEngineers >= license.getUserLicenses().getNoOfWellEngineer()) {
						return "Error:Reached Maximum limit of Well Engineers. Please upgrade License.";
					}
				default:
					if (noOfOtherUsers >= license.getUserLicenses().getNoOfOtherUser()) {
						return "Error:Reached Maximum limit of Other Users. Please upgrade License.";
					}
				}
				userRepository.save(login);
				return "Success:" + login.getId();
			} else {
				return "Error:Your License is not set";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/addProject", method = RequestMethod.POST, produces = "application/json")
	public String addProject(@RequestBody Project project) {
		try {
			projectRepository.save(project);
			return mapper.writeValueAsString(project);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/addWell", method = RequestMethod.POST, produces = "application/json")
	public String addWell(@RequestBody Well well) {
		try {
			wellRepository.save(well);
			Project project = projectRepository.findById(well.getProjectId());
			project.incrementNoOfWells();
			projectRepository.save(project);
			return mapper.writeValueAsString(well);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/deleteClient", method = RequestMethod.GET, produces = "application/json")
	public String deleteClient(@RequestParam("clientId") String clientId) {
		try {
			Client client = clientRepository.findById(clientId);
			Login user = userRepository.findByUserId(client.getUserId());
			clientRepository.delete(client);
			userRepository.delete(user);
			return "Success:Done";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/deleteUser", method = RequestMethod.GET, produces = "application/json")
	public String deleteUser(@RequestParam("clientId") String clientId, @RequestParam("userId") String userId) {
		try {
			Login user = userRepository.findByUserId(userId);
			String result = "";
			if (user != null && !user.getRole().equals("ROLE_ADMIN") && user.getClientId().equals(clientId)) {
				if (user.getRole().equals("ROLE_WELL_MANAGER")) {
					Project project = projectRepository.findByWellManagerId(userId);
					if (project != null) {
						result = "Error:This User has been Assigned to the Project";
					} else {
						userRepository.delete(user);
						result = "Success:Done";
					}
				} else if (user.getRole().equals("ROLE_WELL_ENGINEER")) {
					Well wellEntity = wellRepository.findByWellEngineerId(userId);
					if (wellEntity != null) {
						result = "Error:This User has been Assigned to the Well";

					} else {
						userRepository.delete(user);
						result = "Success:Done";
					}
				}

			} else {
				result = "Error:Access Denied";
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}

	}

	@RequestMapping(value = "/deleteProject", method = RequestMethod.GET, produces = "application/json")
	public String deleteProject(@RequestParam("clientId") String clientId,
			@RequestParam("projectId") String projectId) {
		try {
			Client client = clientRepository.findById(clientId);
			Project project = projectRepository.findById(projectId);
			if (project.getClientId().equals(client.getId())) {
				projectRepository.delete(project);
				return "Success:Done";
			} else {
				return "Error:Access Denied";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/deleteWell", method = RequestMethod.GET, produces = "application/json")
	public String deleteWell(@RequestParam("clientId") String clientId, @RequestParam("projectId") String projectId,
			@RequestParam("wellId") String wellId) {
		try {
			Project project = projectRepository.findById(projectId);
			Well well = wellRepository.findById(wellId);
			if (project.getClientId().equals(clientId) && well.getProjectId().equals(project.getId())) {
				project.decrementNoOfWells();
				projectRepository.save(project);
				wellRepository.delete(well);
				return "Success:Done";
			} else {
				return "Error:Access Denied";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/updateClient", method = RequestMethod.POST, produces = "application/json")
	public String updateClient(@RequestBody Client client) {
		try {
			clientRepository.save(client);
			return "Success:Done";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/updateUser", method = RequestMethod.POST, produces = "application/json")
	public String updateUser(@RequestBody Login user) {
		try {
			Login newUser = userRepository.findByUserId(user.getUserId());
			if (newUser != null) {
				newUser.setFirstName(user.getFirstName());
				newUser.setLastName(user.getLastName());
				// newUser.setUserId(user.getUserId());
				// newUser.setReportId(user.getReportId());
				userRepository.save(newUser);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/migrateUser", produces = "application/json")
	public String updateUser(@RequestParam("oldUserId") String oldUserId, @RequestParam("newUserId") String newUserId,
			@RequestParam("password") String password, @RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName) {
		try {
			Login newUser = userRepository.findByUserId(oldUserId);

			if (newUser != null) {
				newUser.setFirstName(firstName);
				newUser.setLastName(lastName);
				newUser.setUserId(newUserId);
				newUser.setPassword(bCryptPasswordEncoder.encode(password));
				userRepository.save(newUser);
				id = newUser.getId();

				List<Project> projectList = projectRepository.findByManagerId(id);
				Iterator iter = projectList.iterator();
				while (iter.hasNext()) {
					Project project = (Project) iter.next();
					project.setWellManager(newUserId);
					projectRepository.save(project);
				}

				List<Well> wellList = wellRepository.findByEngineerId(id);
				Iterator iterWell = wellList.iterator();
				while (iterWell.hasNext()) {
					Well well = (Well) iterWell.next();
					well.setWellEngineer(newUserId);
					wellRepository.save(well);
				}

				return "Success:Done";

			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/updateUserCredential", method = RequestMethod.POST, produces = "application/json")
	public String updateUser(@RequestParam("userId") String userId, @RequestParam("password") String password) {
		try {
			Login newUser = userRepository.findByUserId(userId);
			if (newUser != null) {
				newUser.setPassword(bCryptPasswordEncoder.encode(password));
				userRepository.save(newUser);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/updateProject", method = RequestMethod.POST, produces = "application/json")
	public String updateProject(@RequestBody Project project) {
		try {
			projectRepository.save(project);
			return "Success:Done";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/archiveWell", method = RequestMethod.POST, produces = "application/json")
	public String archiveWell(@RequestBody Well well) {
		try {
			Well inWell = wellRepository.findById(well.getId());
			if (inWell != null) {
				if (!(inWell.getProjectId()).equals(well.getProjectId())) {
					Project project = projectRepository.findById(inWell.getProjectId());
					if (project.getClientId().equals(inWell.getClientId())
							&& inWell.getProjectId().equals(project.getId())) {
						project.decrementNoOfWells();
						projectRepository.save(project);
					}
					Project project1 = projectRepository.findById(well.getProjectId());
					if (project1 != null) {
						project1.incrementNoOfWells();
						projectRepository.save(project1);
					}
				}
				inWell.setProjectId(well.getProjectId());
				inWell.setWellEngineer(well.getWellEngineer());
//				inWell.setWellLocation(well.getWellLocation());
				inWell.setPlatformName(well.getPlatformName());
				inWell.setClusterName(well.getClusterName());
				inWell.setPadName(well.getPadName());
				inWell.setProjections(well.getProjections());
				inWell.setPadLocation(well.getPadLocation());
				inWell.setWellLocationParameters(well.getWellLocationParameters());
				inWell.setDatum(well.getDatum());
				inWell.setWellType(well.getWellType());
				inWell.setWellLandscape(well.getWellLandscape());
				inWell.setWaterDepth(well.getWaterDepth());
				inWell.setRigType(well.getRigType());
				inWell.setRigName(well.getRigName());
				inWell.setRigContractor(well.getRigContractor());
				inWell.setAirGap(well.getAirGap());
				inWell.setWellheadToDatumLength(well.getWellheadToDatumLength());
				inWell.setRkbToDatumLength(well.getRkbToDatumLength());
				inWell.setPlatformDimensions(well.getPlatformDimensions());
				inWell.setSlotNo(well.getSlotNo());
				inWell.setSlotLocation(well.getSlotLocation());
				inWell.setSlotCoordinate(well.getSlotCoordinate());
				// inWell.setSlotCoordinateParameters(well.getSlotCoordinateParameters());
				inWell.setWellArchived(true);

				wellRepository.save(inWell);
				return "Success:done";
			} else {
				return "Error:not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:error";
		}
	}

	@RequestMapping(value = "/updateWell", method = RequestMethod.POST, produces = "application/json")
	public String updateWell(@RequestBody Well well) {
		try {
			Well inWell = wellRepository.findById(well.getId());
			if (inWell != null) {
				if (!(inWell.getProjectId()).equals(well.getProjectId())) {
					Project project = projectRepository.findById(inWell.getProjectId());
					if (project.getClientId().equals(inWell.getClientId())
							&& inWell.getProjectId().equals(project.getId())) {
						project.decrementNoOfWells();
						projectRepository.save(project);
					}
					Project project1 = projectRepository.findById(well.getProjectId());
					if (project1 != null) {
						project1.incrementNoOfWells();
						projectRepository.save(project1);
					}
				}
				inWell.setProjectId(well.getProjectId());
				inWell.setWellEngineer(well.getWellEngineer());
				inWell.setWellName(well.getWellName());
				inWell.setCurrency(well.getCurrency());
				inWell.setWellUnits(well.getWellUnits());
				inWell.setDateFormat(well.getDateFormat());
				inWell.setOldWell(well.getOldWell());
				inWell.setPlannedDate(well.getPlannedDate());
				inWell.setSpudDate(well.getSpudDate());
				inWell.setCompletedDate(well.getCompletedDate());
				inWell.setDatum(well.getDatum());
				inWell.setCustomDatum(well.getCustomDatum());
//				inWell.setWellLocation(well.getWellLocation());
				inWell.setPlatformName(well.getPlatformName());
				inWell.setPlatformDimensions(well.getPlatformDimensions());
				inWell.setWellLandscape(well.getWellLandscape());// landscape(Platform,Subsea,Standalone)
				inWell.setProjections(well.getProjections());
				inWell.setWellLocationParameters(well.getWellLocationParameters());
				inWell.setWellType(well.getWellType());// onshorfe,offshore
				inWell.setWaterDepth(well.getWaterDepth());
				inWell.setRigType(well.getRigType());
				inWell.setRigName(well.getRigName());
				inWell.setRigContractor(well.getRigContractor());
				inWell.setRigOperator(well.getRigOperator());
				inWell.setAirGap(well.getAirGap());
				inWell.setWellheadToDatumLength(well.getWellheadToDatumLength());
				inWell.setRkbToDatumLength(well.getRkbToDatumLength());
				inWell.setPlatformSlotLocation(well.getPlatformSlotLocation());
				inWell.setPlatformSlotCoordinate(well.getPlatformSlotCoordinate());
				inWell.setPlatformSlotCoordinateParameters(well.getPlatformSlotCoordinateParameters());
				inWell.setRkbfromDatumLength(well.getRkbfromDatumLength());
				inWell.setGroundElevationToMsl(inWell.getGroundElevationToMsl());
				inWell.setRigOnLocationDate(well.getRigOnLocationDate());
				inWell.setRigAcceptanceDate(well.getRigAcceptanceDate());
				inWell.setRigOnWellDate(well.getRigOnWellDate());
				inWell.setRigSpudDate(well.getRigSpudDate());
				inWell.setDrillingMode(well.getDrillingMode());
				inWell.setPlannedMd(well.getPlannedMd());
				inWell.setPlannedTvd(well.getPlannedTvd());
				inWell.setPlannedWellTime(well.getPlannedWellTime());
				inWell.setPlannedWellCost(well.getPlannedWellCost());
				inWell.setActualMd(well.getActualMd());
				inWell.setActualTvd(well.getActualTvd());
				inWell.setActualWellTime(well.getActualWellTime());
				inWell.setActualWellCost(well.getActualWellCost());
				inWell.setEngineerId(well.getEngineerId());
				inWell.setDepthUnit(well.getDepthUnit());
				inWell.setDiameterUnit(well.getDiameterUnit());
				inWell.setDrillingUnit(well.getDrillingUnit());
				inWell.setVolumeUnit(well.getVolumeUnit());
				inWell.setVelocityUnit(well.getVelocityUnit());
				inWell.setDisplacementUnit(well.getDisplacementUnit());
				inWell.setGeneralWeightUnit(well.getGeneralWeightUnit());
				inWell.setTorqueUnit(well.getTorqueUnit());
				inWell.setStringWeightUnit(well.getStringWeightUnit());
				inWell.setRihPoohSpeedUnit(well.getRihPoohSpeedUnit());
				inWell.setRihPoohTimeUnit(well.getRihPoohTimeUnit());
				inWell.setPumpOutputUnit(well.getPumpOutputUnit());
				inWell.setPressureUnit(well.getPressureUnit());
				inWell.setJetVelocityUnit(well.getJetVelocityUnit());
				inWell.setFlowRateUnit(well.getFlowRateUnit());
				inWell.setSlotNo(well.getSlotNo());
				inWell.setMudWeightUnit(well.getMudWeightUnit());
				inWell.setCurrencyWithSymbol(well.getCurrencyWithSymbol());
				inWell.setWellUnitText(well.getWellUnitText());
				inWell.setWellEngineerId(well.getWellEngineerId());
				inWell.setReferenceNumber(well.getReferenceNumber());
				inWell.setReferenceType(well.getReferenceType());
				inWell.setImage_url(well.getImage_url());
				// inWell.setSlotCoordinateParameters(well.getSlotCoordinateParameters());

				wellRepository.save(inWell);
				return mapper.writeValueAsString(inWell);
			} else {
				return "Error:not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:error";
		}
	}

	@RequestMapping(value = "/verifyUserExists", produces = "application/json")
	public String verifyUserExists(@RequestParam("userId") String userId) {
		try {
			Login user = userRepository.findByUserId(userId);

			return "Success:" + (user != null);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:False";
		}
	}

	@RequestMapping(value = "/verifyWellExists", produces = "application/json")
	public String verifyWellExists(@RequestParam("clientId") String clientId,
			@RequestParam("wellName") String wellName) {
		try {
			List<Well> wells = wellRepository.findByClientId(clientId);
			boolean isWellExist = false;

			Iterator<Well> itr = wells.iterator();

			while (itr.hasNext()) {
				Well well = itr.next();
				if (well.getWellName().equals(wellName))
					isWellExist = true;
			}

			return "Success:" + isWellExist;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/verifyProjectExists", produces = "application/json")
	public String verifyProjectExists(@RequestParam("clientId") String clientId,
			@RequestParam("projectName") String projectName) {
		try {
			List<Project> projects = projectRepository.findByClientId(clientId);
			boolean isProjectExist = false;

			Iterator<Project> itr = projects.iterator();

			while (itr.hasNext()) {
				Project project = itr.next();
				if (project.getProjectName().equals(projectName))
					isProjectExist = true;
			}

			return "Success:" + isProjectExist;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/getClients", produces = "application/json")
	public String getClients(@RequestParam("clientId") String clientId) {
		try {
			List<Client> clients = clientRepository.findAll();
			JSONArray json = new JSONArray();

			Iterator<Client> itr = clients.iterator();
			while (itr.hasNext()) {
				Client client = itr.next();
				JSONObject jObj = new JSONObject();
//				System.out.println("clientId-----"+ clientId);
//				System.out.println("clientIdgetId-----"+ client.getId());
				if ((clientId != "" && clientId.equalsIgnoreCase(client.getId())) || clientId == "") {
//					System.out.println("clientId"+ clientId);
					Login login = userRepository.findByUserId(client.getUserId());
					jObj.put("id", client.getId());
					jObj.put("name", client.getCompanyName());
					jObj.put("emailId", client.getUserId());
					jObj.put("contactNumber", client.getContactNumber());
					jObj.put("country", client.getCountry());
					jObj.put("requestedWells", client.getNoOfWells());
					jObj.put("requestedProjects", client.getNoOfProjects());
					jObj.put("requestedLicenseType", client.getLicenseType());
					jObj.put("firstName", login.getFirstName());
					jObj.put("lastName", login.getLastName());

					if (client.getLicenseType().equalsIgnoreCase("custom")) {
						jObj.put("requestedUsers", client.getNoOfUsers());
						jObj.put("requested_noOfWellManager", client.getNoOfWellManager());
						jObj.put("requested_noOfWellEngineer", client.getNoOfWellEngineer());
						jObj.put("requested_noOfOtherUser", client.getNoOfOtherUser());
					} else {
						jObj.put("requestedUsers", client.getNoOfUsers());
					}

					License license = licenseRepository.findByClientId(client.getId());

					if (license != null) {
						jObj.put("noOfWells", license.getNoOfWells());
						UserLicense uL = license.getUserLicenses();
						if (uL != null) {
							jObj.put("noOfUsers", uL.getNoOfUsers());
							jObj.put("noOfWellManager", uL.getNoOfWellManager());
							jObj.put("noOfWellEngineer", uL.getNoOfWellEngineer());
							jObj.put("noOfOtherUser", uL.getNoOfOtherUser());
						} else {
							jObj.put("noOfUsers", "1");
						}
						jObj.put("hasLicense", true);
					} else {
						jObj.put("noOfWells", "0");
						jObj.put("noOfUsers", "1");
						jObj.put("hasLicense", false);
					}

					List<Project> projects = projectRepository.findByClientId(client.getId());
					jObj.put("noOfProjects", projects.size());

					List<Well> wells = wellRepository.findByClientId(client.getId());
					jObj.put("noOfCurrentWells", wells.size());

					int noOfOldWells = 0;
					int noOfNewWells = 0;
					Iterator<Well> itr1 = wells.iterator();

					while (itr1.hasNext()) {
						Well well = itr1.next();
						if (well.getOldWell() != null && well.getOldWell().equals("true")) {
							noOfOldWells++;
						} else
							noOfNewWells++;
					}
					jObj.put("licensed_noOfOldWells", client.getNoOfOldWells());
					jObj.put("noOfOldWells", noOfOldWells);
					jObj.put("noOfNewWells", noOfNewWells);

					List<Login> users = userRepository.findByClientId(client.getId());
					int noOfWellManagers = 0;
					int noOfWellEngineers = 0;
					int noOfOtherUsers = 0;

					Iterator<Login> itr2 = users.iterator();
					while (itr2.hasNext()) {
						Login lUser = itr2.next();
						if (lUser.getRole().equals("ROLE_WELL_MANAGER"))
							noOfWellManagers++;
						if (lUser.getRole().equals("ROLE_WELL_ENGINEER"))
							noOfWellEngineers++;
						if (lUser.getRole().equals("ROLE_OTHER_USER"))
							noOfOtherUsers++;
					}

					jObj.put("noOfCurrentWellManagers", noOfWellManagers);
					jObj.put("noOfCurrentWellEngineers", noOfWellEngineers);
					jObj.put("noOfCurrentOtherUsers", noOfOtherUsers);

					jObj.put("status", client.getStatus());
					json.put(jObj);
				}
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/getPendingClients", produces = "application/json")
	public String getPendingClients() {
		try {
			List<Client> clients = clientRepository.findAll();
			JSONArray json = new JSONArray();

			Iterator<Client> itr = clients.iterator();
			while (itr.hasNext()) {
				Client client = itr.next();
				if (client.getStatus().equals("pending")) {
					JSONObject jObj = new JSONObject();
					jObj.put("id", client.getId());
					jObj.put("name", client.getCompanyName());
					json.put(jObj);
				}
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/activateClient", produces = "application/json")
	public String activateClient(@RequestParam("clientId") String clientId) throws JSONException {
		try {
			Client client = clientRepository.findById(clientId);
			Login login = userRepository.findByUserId(client.getUserId());
			client.setStatus("approved");
			clientRepository.save(client);
			String message = "IwellsBenchmarking Notice: Registration Notice";
			String body = "<h3>Hi " + " " + login.getFirstName() + login.getLastName() + " ,</h3>"
					+ " <p>your account has been activated.</p>";
			// String reportId=login.getReportId();
			String adminEmail = client.getUserId();
			UserServiceImpl userService = new UserServiceImpl();
			userService.singleEmail(body, adminEmail, message);

			return "Success:" + client.getStatus();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/getProjects", produces = "application/json")
	public String getProjects(@RequestParam("clientId") String clientId) {
		try {
			List<Project> projects = projectRepository.findByClientId(clientId);
			JSONArray json = new JSONArray();

			Iterator<Project> itr = projects.iterator();
			while (itr.hasNext()) {
				Project project = itr.next();
				JSONObject jObj = new JSONObject();
				jObj.put("id", project.getId());
				jObj.put("name", project.getProjectName());
				jObj.put("noOfWells", project.getNoOfWells());
				jObj.put("wellManager", project.getWellManager());
				jObj.put("wellManagerId", project.getWellManagerId());
				json.put(jObj);
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/getProject", produces = "application/json")
	public String getProject(@RequestParam("clientId") String clientId, @RequestParam("projectId") String projectId) {
		try {
			Project project = projectRepository.findById(projectId);

			if (project.getClientId().equals(clientId)) {
				return mapper.writeValueAsString(project);
			} else
				return "{}";
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	@RequestMapping(value = "/getWell", produces = "application/json")
	public String getWell(@RequestParam("clientId") String clientId, @RequestParam("wellId") String wellId) {
		try {
			Well well = wellRepository.findById(wellId);

			if (well.getClientId().equals(clientId)) {
				return mapper.writeValueAsString(well);
			} else
				return "{}";
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	@RequestMapping(value = "/getUser", produces = "application/json")
	public String getUser(@RequestParam("clientId") String clientId, @RequestParam("userId") String userId) {
		try {
			Login user = userRepository.findByUserId(userId);

			if (user.getClientId().equals(clientId)) {
				return mapper.writeValueAsString(user);
			} else
				return "{}";
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	@RequestMapping(value = "/getWells", produces = "application/json")
	public String getWells(@RequestParam("clientId") String clientId) {
		try {
			List<Well> wells = wellRepository.findByClientId(clientId);
			JSONArray json = new JSONArray();

			Iterator<Well> itr = wells.iterator();
			while (itr.hasNext()) {
				Well well = itr.next();
				Project project = projectRepository.findById(well.getProjectId());
				JSONObject jObj = new JSONObject();
				jObj.put("id", well.getId());
				jObj.put("name", well.getWellName());
				jObj.put("projectName", project.getProjectName());
				jObj.put("wellManager", project.getWellManager());
				jObj.put("wellEngineer", well.getWellEngineer());
				jObj.put("oldWell", well.getOldWell());
				jObj.put("wellType", well.getWellType());
				jObj.put("wellLandscape", well.getWellLandscape());
				jObj.put("isWellArchvied", well.isWellArchived());
				// jObj.put("wellDate", well.getStartDate().getTime());
				jObj.put("wellDate", new Date().getTime());
				JSONArray wellInputs = new JSONArray(well.getWellInputs());
				if (wellInputs.length() == 0) {
					jObj.put("isWellInputHasData", false);
				} else {
					jObj.put("isWellInputHasData", true);
				}
				jObj.put("isWellInputArchvied", well.isWellInputArchived());
				json.put(jObj);
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/getAllUsers", produces = "application/json")
	public String getAllUsers(@RequestParam("clientId") String clientId) {
		try {
			List<Login> users = userRepository.findByClientId(clientId);
			JSONArray json = new JSONArray();

			Iterator<Login> itr = users.iterator();
			while (itr.hasNext()) {
				Login user = itr.next();
				if (!user.getRole().equals("ROLE_ADMIN")) {

					JSONObject jObj = new JSONObject();
					jObj.put("id", user.getId());
					jObj.put("name", user.getFirstName() + " " + user.getLastName());
					jObj.put("role", user.getRole());
					jObj.put("userId", user.getUserId());
					json.put(jObj);
				}
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/getUsers", produces = "application/json")
	public String getUsers(@RequestParam("clientId") String clientId, @RequestParam("userId") String userId) {
		try {
			JSONArray json = new JSONArray();
			Login mUser = userRepository.findByUserId(userId);
			List<Login> users = userRepository.findByClientId(clientId);
			Iterator<Login> itr = users.iterator();
			while (itr.hasNext()) {
				Login user = itr.next();
				if (user.getReportId().equals(userId) || mUser.getRole().equals("ROLE_ADMIN")) {
					JSONObject jObj = new JSONObject();
					jObj.put("id", user.getId());
					jObj.put("name", user.getFirstName() + " " + user.getLastName());
					jObj.put("role", user.getRole());
					json.put(jObj);
				}
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "[]";
		}
	}

	@RequestMapping(value = "/getUserDetails", produces = "application/json")
	public String getUserDetails(@RequestParam("clientId") String clientId, @RequestParam("userId") String userId) {
		try {
			Client client = clientRepository.findById(clientId);
			Login user = userRepository.findByUserId(userId);
			if (user.getClientId().equals(client.getId())) {
				JSONObject jObj = new JSONObject();
				jObj.put("id", user.getId());
				jObj.put("firstName", user.getFirstName());
				jObj.put("lastName", user.getLastName());
				jObj.put("reportId", user.getReportId());
				jObj.put("role", user.getRole());
				return jObj.toString();
			} else {
				return "{}";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	@RequestMapping(value = "/getUserRoles", produces = "application/json")
	public String getUserRoles() {
		List<Role> roles = roleRepository.findAll();
		JSONArray json = new JSONArray();

		Iterator<Role> itr = roles.iterator();
		while (itr.hasNext()) {
			Role role = itr.next();
			json.put(role.getRoleName());
		}
		return json.toString();
	}

	@RequestMapping(value = "/convertWellInputJSONtoCSV", method = RequestMethod.POST, produces = "application/json")
	public String convertWellInputJSONtoCSV(@RequestParam("wellInputJson") String wellInputJson,
			@RequestParam("level") String level) throws IOException {

		String wellInputStr = "";
//		JSONArray jAr = new JSONArray(wellInputJson);
		if (level.equals("1")) {
//			wellInputStr = Utils.convertWellInputCSVToObj1(Utils.toCSV(jAr));
			wellInputStr = Utils.convertWellInputObjToCSV_1(wellInputJson);
//			wellInputStr = Utils.convertWellInputCSVToObj1(Utils.toCSV(jAr));
		} else if (level.equals("2")) {
			wellInputStr = Utils.convertWellInputObjToCSV_2(wellInputJson);
		} else if (level.equals("3")) {
			wellInputStr = Utils.convertWellInputObjToCSV_3(wellInputJson);
		} else if (level.equals("4")) {
			wellInputStr = Utils.convertWellInputObjToCSV_4(wellInputJson);
		} else {
			wellInputStr = Utils.convertWellInputObjToCSV_5(wellInputJson);
		}
		return wellInputStr;

	}

	@RequestMapping(value = "/readWellInputCSVFile", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
	public String convertWellInputCSVToJSON(@RequestParam("file") MultipartFile uploadfile,
			@RequestParam("level") String level) throws IOException {
		try {
			return Utils.convertWellInputFileToObjtest(Arrays.asList(uploadfile), level);

		} catch (Exception e) {
			return "Error:File should be in the specified format";
		}
	}

	@RequestMapping(value = "/testConvertWellInputJSONtoCSV", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String testConvertWellInputToCSV(@RequestParam("wellInputJson") String wellInputJson,
			@RequestParam("level") String level) throws IOException {

		String wellInputStr = "";
		if (level.equals("1")) {

			wellInputStr = TestUtils.testconvertWellInputObjToCSV_1(wellInputJson);

		} else if (level.equals("2")) {
			wellInputStr = TestUtils.testconvertWellInputObjToCSV_2(wellInputJson);
		} else if (level.equals("3")) {
			wellInputStr = TestUtils.testconvertWellInputObjToCSV_3(wellInputJson);
		} else if (level.equals("4")) {
			wellInputStr = TestUtils.testconvertWellInputObjToCSV_4(wellInputJson);
		} else {
			wellInputStr = TestUtils.testconvertWellInputObjToCSV_5(wellInputJson);
		}
		return wellInputStr;

	}

	@RequestMapping(value = "/testreadWellInputCSVFile", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
	public String testconvertWellInputCSVToJSON(@RequestParam("file") MultipartFile uploadfile,
			@RequestParam("level") String level) throws IOException {
		try {
			return TestUtils.convertWellInputFileToObjtest(Arrays.asList(uploadfile), level);

		} catch (Exception e) {
			return "Error:File should be in the specified format";
		}
	}

	@RequestMapping(value = "/addWellInput", method = RequestMethod.POST, produces = "application/json")
	public String addWellInput(@RequestBody WellInputJson wellInputJson) {
		try {
			Well well = wellRepository.findById(wellInputJson.getWellId());
			//Login user = userRepository.findByUserId(wellInputJson.getUserId());
			if (well != null) {
//				if (user != null
//						&& (user.getRole().equals("ROLE_WELL_ENGINEER") || user.getRole().equals("ROLE_OTHER_USER"))
//						&& well.isWellInputArchived())
//					return "Error:Well Input is archived. Cannot make changes";
//				JSONArray jAr = new JSONArray(wellInputJson.getWellInputJson());
//				if (wellInputJson.getWellInputLevel().equals("1")) {
//					String wellInputStr = Utils.convertWellInputCSVToObj1(Utils.toCSV(jAr));
//					well.setWellInputs(wellInputStr);
//				} else if (wellInputJson.getWellInputLevel().equals("2")) {
//					String wellInputStr = Utils.convertWellInputCSVToObj2(Utils.toCSV(jAr));
//					well.setWellInputs(wellInputStr);
//				}
//				if (wellInputJson.getWellInputLevel().equals("3")) {
//					String wellInputStr = Utils.convertWellInputCSVToObj(Utils.toCSV(jAr));
//					well.setWellInputs(wellInputStr);
//				} else if (wellInputJson.getWellInputLevel().equals("4")) {
//					String wellInputStr = Utils.convertWellInputCSVToObj4(Utils.toCSV(jAr));
//					well.setWellInputs(wellInputStr);
//				} else {
//					String wellInputStr = Utils.convertWellInputCSVToObj5(Utils.toCSV(jAr));
//					well.setWellInputs(wellInputStr);
//				}
				well.setWellInputs(wellInputJson.getWellInputJson());
				well.setWellInputLevel(wellInputJson.getWellInputLevel());
				well.setId(wellInputJson.getWellId());
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}
	@RequestMapping(value = "/updateBaseWellConfiguration", method = RequestMethod.POST, produces = "application/json")
	public String updateBaseWellConfiguration(@RequestBody WellConfiguration wellConfig) {
		try {
		WellConfiguration config = wellConfigurationRepository.findByWellIdAndLevel(wellConfig.getWellId(), wellConfig.getLevel());
		if(config != null) {
			config.setAfeComponentsLocked(false);
			config.setAverageComponents(wellConfig.getAverageComponents());
			config.setConfigNo(wellConfig.getConfigNo());
			config.setConfigSaved(false);
			config.setConfigSubmitted(false);
			config.setLevel(wellConfig.getLevel());
			config.setTaxAndLevyData(wellConfig.getTaxAndLevyData());
			config.setWellConfig(wellConfig.getWellConfig());
			config.setWellEstimateDays(wellConfig.getWellEstimateDays());
			config.setWellInclineDays(wellConfig.getWellInclineDays());
			config.setWellId(wellConfig.getWellId());
			config.setWellTimelineLocked(false);
			wellConfigurationRepository.save(config);
			return "Success:Done";
		}
		else {
			return "Success:Entity Not Found";
		}
		
		
	}
		catch(Exception e) {
			e.printStackTrace();
			return "Error:Not saved";
		}
	}
	@RequestMapping(value = "/addWellConfiguration", method = RequestMethod.POST, produces = "application/json")
	public String addWellConfiguration(@RequestBody WellConfiguration wellConfig) {
		try {
		WellConfiguration config = new WellConfiguration();
		config.setLevel(wellConfig.getLevel());
		config.setConfigNo(wellConfig.getConfigNo());
		config.setWellId(wellConfig.getWellId());
		config.setWellConfig(wellConfig.getWellConfig());
		wellConfigurationRepository.save(config);
		return "Success:Done";
		
	}
		catch(Exception e) {
			e.printStackTrace();
			return "Error:Not saved";
		}
	}
	
	@RequestMapping(value = "/updateWellConfiguration", method = RequestMethod.POST, produces = "application/json")
	public String updateWellConfiguration(@RequestBody WellConfiguration wellConfig) {
		try {
		WellConfiguration config = wellConfigurationRepository.findById(wellConfig.getId());
		if(config != null) {
//			WellEstimateDays wellDays = new WellEstimateDays();
//			wellDays.setPhase(wellConfig.getWellEstimateDays().size());
			config.setLevel(wellConfig.getLevel());
			config.setConfigNo(wellConfig.getConfigNo());
			config.setWellId(wellConfig.getWellId());
			config.setWellConfig(wellConfig.getWellConfig());
			config.setWellEstimateDays(wellConfig.getWellEstimateDays());
			wellConfigurationRepository.save(config);
				
		}
		return "Success:Done";
		
	}
		catch(Exception e) {
			e.printStackTrace();
			return "Error:Not saved";
		}
	}
	@RequestMapping(value = "/submitWellConfiguration", method = RequestMethod.POST, produces = "application/json")
	public String submitWellConfiguration(@RequestBody WellConfiguration wellConfig) {
		try {
		WellConfiguration config = wellConfigurationRepository.findByWellIdLevelAndConfigNo(wellConfig.getWellId(),wellConfig.getLevel(),wellConfig.getConfigNo());
		if(config != null) {
//			WellEstimateDays wellDays = new WellEstimateDays();
//			wellDays.setPhase(wellConfig.getWellEstimateDays().size());
			//WellConfiguration wellConfigEntity = config.get(0);
			
			config.setLevel(wellConfig.getLevel());
			config.setConfigNo(wellConfig.getConfigNo());
			config.setWellId(wellConfig.getWellId());
			config.setWellConfig(wellConfig.getWellConfig());
			config.setWellEstimateDays(wellConfig.getWellEstimateDays());
			config.setConfigSaved(wellConfig.isConfigSaved());
			config.setConfigSubmitted(wellConfig.isConfigSubmitted());
			config.setTaxAndLevyData(wellConfig.getTaxAndLevyData());
			config.setWellInclineDays(wellConfig.getWellInclineDays());
			config.setSelectedComponents(wellConfig.getSelectedComponents());
			config.setAfeComponentsLocked(wellConfig.isAfeComponentsLocked());
			config.setWellTimelineLocked(wellConfig.isWellTimelineLocked());
			config.setAverageComponents(wellConfig.getAverageComponents());
			config.setLastModifiedDate(new Date());
			wellConfigurationRepository.save(config);
				
		}
		else {
			WellConfiguration wellConfigEntity1 = new WellConfiguration();
			
			wellConfigEntity1.setLevel(wellConfig.getLevel());
			wellConfigEntity1.setConfigNo(wellConfig.getConfigNo());
			wellConfigEntity1.setWellId(wellConfig.getWellId());
			wellConfigEntity1.setWellConfig(wellConfig.getWellConfig());
			wellConfigEntity1.setWellEstimateDays(wellConfig.getWellEstimateDays());
			wellConfigEntity1.setConfigSaved(wellConfig.isConfigSaved());
			wellConfigEntity1.setConfigSubmitted(wellConfig.isConfigSubmitted());
			wellConfigEntity1.setTaxAndLevyData(wellConfig.getTaxAndLevyData());
			wellConfigEntity1.setWellInclineDays(wellConfig.getWellInclineDays());
			wellConfigEntity1.setAfeComponentsLocked(wellConfig.isAfeComponentsLocked());
			wellConfigEntity1.setWellTimelineLocked(wellConfig.isWellTimelineLocked());
			wellConfigEntity1.setSelectedComponents(wellConfig.getSelectedComponents());
			wellConfigEntity1.setAverageComponents(wellConfig.getAverageComponents());
			wellConfigEntity1.setLastModifiedDate(new Date());

			wellConfigurationRepository.save(wellConfigEntity1);
		}
		return "Success:Done";
		
	}
		catch(Exception e) {
			e.printStackTrace();
			return "Error:Not saved";
		}
	}
	@RequestMapping(value="/submitWellOperation",produces = "application/json",method=RequestMethod.POST)
	public String submitWellOperation(@RequestBody RealTimeWellOperation wellOperation){
		try {
			RealTimeWellOperation entity = realTimeWellOperationRepository.findByWellId(wellOperation.getWellId());
			if(entity != null) {
				entity.setLevel(wellOperation.getLevel());
				entity.setLocked(wellOperation.isLocked());
				entity.setWellDaysInput(wellOperation.getWellDaysInput());
				entity.setWellId(wellOperation.getWellId());
				entity.setWellStartDate(wellOperation.getWellStartDate());
				realTimeWellOperationRepository.save(entity);
			}
			else {
				entity = new RealTimeWellOperation();
				entity.setLevel(wellOperation.getLevel());
				entity.setLocked(wellOperation.isLocked());
				entity.setWellDaysInput(wellOperation.getWellDaysInput());
				entity.setWellId(wellOperation.getWellId());
				entity.setWellStartDate(wellOperation.getWellStartDate());
				realTimeWellOperationRepository.save(entity);
			}
			return mapper.writeValueAsString(entity);	
		}
		catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
		
		
	}
	@RequestMapping(value="/submitCostModal",produces = "application/json",method=RequestMethod.POST)
	public String submitCostModal(@RequestBody RealTimeCostModal wellCost){
		try {
			RealTimeCostModal entity = realTimeCostModalRepository.findByWellIdAndDaysValue(wellCost.getWellId(), wellCost.getDaysValue());
			if(entity != null) {
				entity.setWellId(wellCost.getWellId());
				entity.setDaysValue(wellCost.getDaysValue());
				entity.setRealTimeTotalCostArray(wellCost.getRealTimeTotalCostArray());
//				RealTimeTotalCost realTimeTotalCost = new RealTimeTotalCost();
//				realTimeTotalCost.setDaysValue("");
//				realTimeTotalCost.setRealTimeTotalCostArray("[]");
//				entity.addRealTimeTotalCost(realTimeTotalCost);
				realTimeCostModalRepository.save(entity);

			}
			else {
				entity = new RealTimeCostModal();
				entity.setWellId(wellCost.getWellId());
				entity.setDaysValue(wellCost.getDaysValue());
				entity.setRealTimeTotalCostArray(wellCost.getRealTimeTotalCostArray());
				realTimeCostModalRepository.save(entity);
			}
			return mapper.writeValueAsString(entity);	
		}
		catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
		
		
	}
	@RequestMapping(value="/getCostModal",method=RequestMethod.GET,produces="application/json")
	public String getCostModal(@RequestParam("wellId") String wellId,@RequestParam("daysValue") String daysValue){
		try {
			RealTimeCostModal entity = realTimeCostModalRepository.findByWellIdAndDaysValue(wellId, daysValue);
			JSONArray array = new JSONArray();
			JSONObject  object = new JSONObject();
			if(entity != null) {
				object.put("wellId", entity.getWellId());
				object.put("realTimeTotalCostArray", entity.getRealTimeTotalCostArray());
				object.put("daysValue", entity.getDaysValue());
				array.put(object);
			}
			
			return array.toString();
		}
		catch(Exception e) {
			e.printStackTrace();
			return wellId;
		}
	}
	
	@RequestMapping(value="/getWellOperation",method=RequestMethod.GET,produces="application/json")
	public String getWellOperation(@RequestParam("wellId") String wellId){
		try {
			RealTimeWellOperation entity = realTimeWellOperationRepository.findByWellId(wellId);
			JSONArray array = new JSONArray();
			JSONObject  object = new JSONObject();
			if(entity != null) {
				object.put("wellId", entity.getWellId());
				object.put("level", entity.getLevel());
				object.put("wellDaysInput", entity.getWellDaysInput());
				object.put("isLocked", entity.isLocked());
				object.put("wellStartDate", entity.getWellStartDate());

				array.put(object);
			}
			
			return array.toString();
		}
		catch(Exception e) {
			e.printStackTrace();
			return wellId;
		}
	}
	@RequestMapping(value = "/getSingleWellConfiguration", produces = "application/json")
	public String getSingleWellConfiguration(@RequestParam("wellId") String wellId,@RequestParam("level") String level) {
		try {
			JSONArray wellResult=new JSONArray();
			if(level == "") {
				List<WellConfiguration> well1 = wellConfigurationRepository.findByWellId(wellId);
				if(!well1.isEmpty()) {
					Iterator<WellConfiguration> itr = well1.iterator();
					while (itr.hasNext()) {
						WellConfiguration wellConfig = itr.next();
						JSONObject wellEntity = new JSONObject();
						wellEntity.put("wellId",wellConfig.getWellId());
						wellEntity.put("levelValue", wellConfig.getLevel());
						wellEntity.put("configNumber", wellConfig.getConfigNo());
						wellEntity.put("wellConfig", wellConfig.getWellConfig());
						wellEntity.put("wellEstimateDays", wellConfig.getWellEstimateDays());
						wellEntity.put("isconfigSaved", wellConfig.isConfigSaved());
						wellEntity.put("isconfigSubmitted", wellConfig.isConfigSubmitted());
						wellEntity.put("taxAndLevyData", wellConfig.getTaxAndLevyData());
						wellEntity.put("lastModifiedDate", wellConfig.getLastModifiedDate());
						wellEntity.put("wellInclineDays", wellConfig.getWellInclineDays());
						wellEntity.put("averageComponents", wellConfig.getAverageComponents());
						wellEntity.put("selectedComponents", wellConfig.getSelectedComponents());
						wellEntity.put("isAfeComponentsLocked", wellConfig.isAfeComponentsLocked());
						wellEntity.put("iswellTimelineLocked", wellConfig.isWellTimelineLocked());
						wellEntity.put("isconfigSaved", wellConfig.isConfigSaved());


						if(wellId == "") {
							wellEntity.put("isFreshLevel", true);
						}
						else {
							if(wellConfig.getWellId() == "") {
								wellEntity.put("isFreshLevel", true);
							}
							else {
								wellEntity.put("isFreshLevel", false);
							}
						}
						wellResult.put(wellEntity);
					}
				}
			}
			else {
				WellConfiguration well2 = wellConfigurationRepository.findByWellIdAndLevel(wellId,level);
				JSONObject wellEntity = new JSONObject();
				wellEntity.put("wellId",well2.getWellId());
				wellEntity.put("levelValue", well2.getLevel());
				wellEntity.put("configNumber", well2.getConfigNo());
				wellEntity.put("wellConfig", well2.getWellConfig());
				wellEntity.put("wellEstimateDays", well2.getWellEstimateDays());
				wellEntity.put("isconfigSaved", well2.isConfigSaved());
				wellEntity.put("isconfigSubmitted", well2.isConfigSubmitted());
				wellEntity.put("taxAndLevyData", well2.getTaxAndLevyData());
				wellEntity.put("lastModifiedDate", well2.getLastModifiedDate());
				wellEntity.put("wellInclineDays", well2.getWellInclineDays());
				wellEntity.put("averageComponents", well2.getAverageComponents());
				wellEntity.put("selectedComponents", well2.getSelectedComponents());
				wellEntity.put("isAfeComponentsLocked", well2.isAfeComponentsLocked());
				wellEntity.put("iswellTimelineLocked", well2.isWellTimelineLocked());
				wellEntity.put("isconfigSaved", well2.isConfigSaved());


				if(wellId == "") {
					wellEntity.put("isFreshLevel", true);
				}
				else {
					if(well2.getWellId() == "") {
						wellEntity.put("isFreshLevel", true);
					}
					else {
						wellEntity.put("isFreshLevel", false);
					}
				}
				wellResult.put(wellEntity);
			}
			
			
			return wellResult.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
		
		
	}
	@RequestMapping(value = "/deleteWellConfiguration", produces = "application/json")
	public String deleteWellConfiguration(@RequestParam("wellId") String wellId,@RequestParam("level") String level,@RequestParam("config") String config) {
		
		try{
			WellConfiguration wellEntity1 = wellConfigurationRepository.findByWellIdLevelAndConfigNo(wellId,level,config);
			WellConfiguration freshWellEntity = wellConfigurationRepository.findByWellIdLevelAndConfigNo("",level,config);
			JSONArray wellResult=new JSONArray();
			JSONObject wellEntity = new JSONObject();

			if(wellEntity1 != null && freshWellEntity != null) {
				wellEntity.put("wellId",freshWellEntity.getWellId());
				wellEntity.put("levelValue", freshWellEntity.getLevel());
				wellEntity.put("configNumber", freshWellEntity.getConfigNo());
				wellEntity.put("wellConfig", freshWellEntity.getWellConfig());
				wellEntity.put("wellEstimateDays", freshWellEntity.getWellEstimateDays());
				wellEntity.put("isconfigSaved", freshWellEntity.isConfigSaved());
				wellEntity.put("isconfigSubmitted", freshWellEntity.isConfigSubmitted());
				wellEntity.put("taxAndLevyData", freshWellEntity.getTaxAndLevyData());
				wellEntity.put("wellInclineDays", freshWellEntity.getWellInclineDays());
				wellEntity.put("iswellTimelineLocked", freshWellEntity.isWellTimelineLocked());
				wellEntity.put("isAfeComponentsLocked", freshWellEntity.isAfeComponentsLocked());
				wellEntity.put("selectedComponents", freshWellEntity.getSelectedComponents());
				wellEntity.put("averageComponents", freshWellEntity.getAverageComponents());
				wellEntity.put("isFreshLevel", true);

				wellResult.put(wellEntity);
				wellConfigurationRepository.delete(wellEntity1);
			}
			return wellResult.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
		
	}
	@RequestMapping(value = "/getWellConfiguration", produces = "application/json")
	public String getWellConfiguration(@RequestParam("wellId") String wellId,@RequestParam("level") String level,@RequestParam("config") String config) {
		try {
			WellConfiguration well = wellConfigurationRepository.findByWellIdLevelAndConfigNo(wellId,level,config);
			String wellId1;
			JSONArray wellResult=new JSONArray();
			if(well != null) {
					WellConfiguration wellConfig = well;
					JSONObject wellEntity = new JSONObject();
					wellEntity.put("wellId",wellConfig.getWellId());
					wellEntity.put("levelValue", wellConfig.getLevel());
					wellEntity.put("configNumber", wellConfig.getConfigNo());
					wellEntity.put("wellConfig", wellConfig.getWellConfig());
					wellEntity.put("wellEstimateDays", wellConfig.getWellEstimateDays());
					wellEntity.put("isconfigSaved", wellConfig.isConfigSaved());
					wellEntity.put("isconfigSubmitted", wellConfig.isConfigSubmitted());
					wellEntity.put("taxAndLevyData", wellConfig.getTaxAndLevyData());
					wellEntity.put("wellInclineDays", wellConfig.getWellInclineDays());
					wellEntity.put("iswellTimelineLocked", wellConfig.isWellTimelineLocked());
					wellEntity.put("isAfeComponentsLocked", wellConfig.isAfeComponentsLocked());
					wellEntity.put("selectedComponents", wellConfig.getSelectedComponents());
					wellEntity.put("averageComponents", wellConfig.getAverageComponents());
					wellEntity.put("lastModifiedDate", wellConfig.getLastModifiedDate());
					wellEntity.put("isFreshLevel", false);

					wellResult.put(wellEntity);
				
			}
			else {
				List<WellConfiguration> well1 = wellConfigurationRepository.findByWellId(wellId);
				List<WellConfiguration> levelIndexCommonArray = new ArrayList<WellConfiguration>();
				//List<String> levelIndexArray = new ArrayList<String>();
				List<WellConfiguration> well2 = wellConfigurationRepository.findByWellId("");
				for(int j = 0;j < well2.size();j++) {
					for(int k = 0;k < well1.size();k++) {
						if(well1.get(k).getLevel() == well2.get(j).getLevel() && well1.get(k).getConfigNo() == well2.get(j).getConfigNo()) {
							levelIndexCommonArray.add(well2.get(j));
						}
					}
					
				}
					for(int levelIndex=0;levelIndex < well2.size();levelIndex++) {
						if(!levelIndexCommonArray.contains(well2.get(levelIndex))) {
							well1.add(well2.get(levelIndex));
						}
					}
				//well1.addAll(well2);
				if(!well1.isEmpty()) {
					
				}
				else {
					wellId1 = "";
					well1 = wellConfigurationRepository.findByWellId(wellId1);
				}
				if(!well1.isEmpty()) {
					Iterator<WellConfiguration> itr = well1.iterator();
					while (itr.hasNext()) {
						WellConfiguration wellConfig = itr.next();
						JSONObject wellEntity = new JSONObject();
						wellEntity.put("wellId",wellConfig.getWellId());
						wellEntity.put("levelValue", wellConfig.getLevel());
						wellEntity.put("configNumber", wellConfig.getConfigNo());
						wellEntity.put("wellConfig", wellConfig.getWellConfig());
						wellEntity.put("wellEstimateDays", wellConfig.getWellEstimateDays());
						wellEntity.put("isconfigSaved", wellConfig.isConfigSaved());
						wellEntity.put("isconfigSubmitted", wellConfig.isConfigSubmitted());
						wellEntity.put("taxAndLevyData", wellConfig.getTaxAndLevyData());
						wellEntity.put("lastModifiedDate", wellConfig.getLastModifiedDate());
						wellEntity.put("wellInclineDays", wellConfig.getWellInclineDays());
						wellEntity.put("averageComponents", wellConfig.getAverageComponents());
						wellEntity.put("selectedComponents", wellConfig.getSelectedComponents());
						wellEntity.put("isAfeComponentsLocked", wellConfig.isAfeComponentsLocked());
						wellEntity.put("iswellTimelineLocked", wellConfig.isWellTimelineLocked());
						wellEntity.put("isconfigSaved", wellConfig.isConfigSaved());


						if(wellId == "") {
							wellEntity.put("isFreshLevel", true);
						}
						else {
							if(wellConfig.getWellId() == "") {
								wellEntity.put("isFreshLevel", true);
							}
							else {
								wellEntity.put("isFreshLevel", false);
							}
						}
						wellResult.put(wellEntity);
					}
				}
			}
			return wellResult.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/addTemplate", method = RequestMethod.POST, produces = "application/json")
	public String addWellTemplate(@RequestBody Template template) {
		try {
			Well well = wellRepository.findById(template.getWellId());
			if (!template.isBasicTemplate() && well != null || template.isBasicTemplate()) {
				JSONArray jAr = new JSONArray(template.getTemplate());
//				System.out.println("jar"+jAr);

				Template templateEntity = null;
				if (!template.isBasicTemplate()) {
					templateEntity = templateRepository.findByWellId(template.getWellId());
				} else {
					templateEntity = null;
				}

				if (templateEntity != null) {
					templateEntity.setTemplateName(template.getTemplateName());
					templateEntity.setBasicTemplate(template.isBasicTemplate());
					templateEntity.setClientId(template.getClientId());
					templateEntity.setTemplateLevel(template.getTemplateLevel());
					templateEntity.setBaseTemplateId(template.getBaseTemplateId());
					templateEntity.setBaseTemplateName(template.getBaseTemplateName());
					templateEntity.setWellType(template.getWellType());
					templateEntity.setWellLandscape(template.getWellLandscape());
					templateEntity.setTemplate(template.getTemplate());
					templateEntity.setOldWell(well.getOldWell());
					templateEntity.setWellCategory(well.getWellCategory());
//					if (template.getTemplateLevel().equals("1")) {
//
//						String wellInputStr = Utils.convertWellInputCSVToObj1(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("2")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj2(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("3")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("4")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj4(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else {
//						String wellInputStr = Utils.convertWellInputCSVToObj5(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					}

					// }

					templateEntity.setArchieved(template.isArchieved());
					if (!template.isBasicTemplate()) {
						templateEntity.setWellId(template.getWellId());

					}
					templateRepository.save(templateEntity);
					return "Success:Done";
				} else {
					templateEntity = new Template();
					templateEntity.setTemplateName(template.getTemplateName());
					templateEntity.setBasicTemplate(template.isBasicTemplate());
					templateEntity.setTemplateLevel(template.getTemplateLevel());
					templateEntity.setWellType(template.getWellType());
					templateEntity.setWellLandscape(template.getWellLandscape());
					templateEntity.setTemplate(template.getTemplate());
//					if (template.getTemplateLevel().equals("1")) {
//
//						String wellInputStr = Utils.convertWellInputCSVToObj1(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("2")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj2(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("3")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else if (template.getTemplateLevel().equals("4")) {
//						String wellInputStr = Utils.convertWellInputCSVToObj4(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					} else {
//						String wellInputStr = Utils.convertWellInputCSVToObj5(Utils.toCSV(jAr));
//						templateEntity.setTemplate(wellInputStr);
//
//					}

					templateEntity.setArchieved(template.isArchieved());
					if (!template.isBasicTemplate()) {
						templateEntity.setWellId(template.getWellId());
						templateEntity.setClientId(template.getClientId());
						templateEntity.setBaseTemplateId(template.getBaseTemplateId());
						templateEntity.setBaseTemplateName(template.getBaseTemplateName());
						templateEntity.setOldWell(well.getOldWell());
						templateEntity.setWellCategory(well.getWellCategory());
					}
					templateRepository.save(templateEntity);
					return "Success:Done";
				}
			}

			else {
				return "Error:Not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/getWellInput", produces = "application/json")
	public String getWellInputs(@RequestParam("wellId") String wellId) {
		try {
			Well well = wellRepository.findById(wellId);
			return (well != null) ? well.getWellInputs() : "[]";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/getTemplate", produces = "application/json")
	public String getTemplate(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "wellId", required = false) String wellId,
			@RequestParam(value = "templateName", required = false) String templateName) {
		JSONArray json = new JSONArray();
		try {
			Template template;
			JSONObject jObj = new JSONObject();
			if (wellId != "") {
				template = templateRepository.findByWellId(wellId);
			} else if (id != "") {
				template = templateRepository.findById(id);
			} else {
				template = templateRepository.findByTemplateName(templateName);
			}
//			System.out.println(template);
			if (template != null) {
//				if (template.getTemplateLevel().equals("1")) {
//					jObj.put("template", template.getTemplateAsJSON1());
//				} else if (template.getTemplateLevel().equals("2")) {
//					jObj.put("template", template.getTemplateAsJSON2());
//				} else if (template.getTemplateLevel().equals("3")) {
//					jObj.put("template", template.getTemplateAsJSON());
//				} else if (template.getTemplateLevel().equals("5")) {
//					jObj.put("template", template.getTemplateAsJSON5());
//				} else {
//					jObj.put("template", template.getTemplateAsJSON4());
//				}
				jObj.put("templateLevel", template.getTemplateLevel());
				jObj.put("isArchieved", template.isArchieved());
				jObj.put("baseTemplateId", template.getBaseTemplateId());
				jObj.put("baseTemplateName", template.getBaseTemplateName());
				jObj.put("template", template.getTemplate());
				jObj.put("wellType", template.getWellType());
				jObj.put("wellLandscape", template.getWellLandscape());
				jObj.put("oldWell", template.getOldWell());
				jObj.put("wellCategory", template.getWellCategory());
				jObj.put("isBasicTemplate", template.isBasicTemplate());
				json.put(jObj);
			}
			return json.toString();

		} catch (Exception e) {
			e.printStackTrace();

			return "[]";
		}
	}

	@RequestMapping(value = "/getAllTemplate", produces = "application/json")
	public String getAllTemplate(@RequestParam("clientId") String clientId,
			// @RequestParam("wellType") String wellType, @RequestParam("wellLandscape")
			// String wellLandscape) {
			@RequestParam("wellId") String wellId) {

		try {
			Well well = wellRepository.findById(wellId);
			JSONArray json = new JSONArray();
			List<Template> basicTemplate = templateRepository.findByBasicTemplate(true);
			Iterator<Template> itr1 = basicTemplate.iterator();
			while (itr1.hasNext()) {
				Template template = itr1.next();
				if ((template.getWellCategory().equals(well.getWellCategory()) && template.isBasicTemplate() == true)
						|| (template.getWellCategory().equals(well.getWellCategory())
								&& template.getWellType().equals(well.getWellType())
								&& template.getWellLandscape().equals(well.getWellLandscape()))) {
					JSONObject jObj = new JSONObject();
					jObj.put("id", template.getId());
					jObj.put("templateName", template.getTemplateName());
					jObj.put("isArchieved", template.isArchieved());
					if (template.getWellId() != null) {
						jObj.put("wellId", template.getWellId());
					} else {
						jObj.put("wellId", "");
					}
//					if (template.getTemplateLevel().equals("1")) {
//						jObj.put("template", template.getTemplateAsJSON1());
//					} else if (template.getTemplateLevel().equals("2")) {
//						jObj.put("template", template.getTemplateAsJSON2());
//					} else if (template.getTemplateLevel().equals("3")) {
//						jObj.put("template", template.getTemplateAsJSON());
//					} else if (template.getTemplateLevel().equals("4")) {
//						jObj.put("template", template.getTemplateAsJSON4());
//					} else {
//						jObj.put("template", template.getTemplateAsJSON5());
//					}

					jObj.put("isBasicTemplate", template.isBasicTemplate());
					jObj.put("isArchieved", template.isArchieved());

					json.put(jObj);
				}
			}
			if (!clientId.equals("")) {
				List<Template> template = templateRepository.findByClientIdAndBasicTemplate(clientId, false);
				Iterator<Template> itr = template.iterator();
				while (itr.hasNext()) {
					Template templates = itr.next();
					if (templates.getWellCategory().equals(well.getWellCategory())
							&& templates.getWellType().equals(well.getWellType())
							&& templates.getWellLandscape().equals(well.getWellLandscape())) {
						JSONObject jObj = new JSONObject();
						jObj.put("id", templates.getId());
						jObj.put("templateName", templates.getTemplateName());
						jObj.put("isArchieved", templates.isArchieved());
						jObj.put("clientId", templates.getClientId());
						if (templates.getWellId() != null) {
							jObj.put("wellId", templates.getWellId());
						} else {
							jObj.put("wellId", "");
						}
//						if (templates.getTemplateLevel().equals("1")) {
//							jObj.put("template", templates.getTemplateAsJSON1());
//						} else if (templates.getTemplateLevel().equals("2")) {
//							jObj.put("template", templates.getTemplateAsJSON2());
//						} else if (templates.getTemplateLevel().equals("3")) {
//							jObj.put("template", templates.getTemplateAsJSON());
//						} else if (templates.getTemplateLevel().equals("4")) {
//							jObj.put("template", templates.getTemplateAsJSON4());
//						} else {
//							jObj.put("template", templates.getTemplateAsJSON5());
//						}

						jObj.put("isBasicTemplate", templates.isBasicTemplate());
						jObj.put("isArchieved", templates.isArchieved());

						json.put(jObj);
					}
				}
			}
			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			JSONArray json1 = new JSONArray();
			json1.put("[]");
			return json1.toString();
		}
	}

	@RequestMapping(value = "/deleteWellInput", produces = "application/json")
	public String deleteWellInput(@RequestParam("wellId") String wellId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				well.setWellInputs("[]");
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/RequestLicenseUpdate", method = RequestMethod.POST, produces = "application/json")
	public String RequestLicenseUpdate(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId,
			@RequestParam("typeOfLicense") String typeOfLicense, @RequestParam("noOfUsers") int noOfUsers,
			@RequestParam("noOfWellManager") int noOfWellManager,
			@RequestParam("noOfWellEngineer") int noOfWellEngineer, @RequestParam("noOfOtherUser") int noOfOtherUser,
			HttpServletRequest request, HttpServletResponse response) {
		System.out.println("userId:" + userId);

		Login user = userRepository.findByUserId(userId);
		System.out.println("user:" + user);
		String context = "";
		context = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		if (user != null && user.getRole().equals("ROLE_ADMIN")) {
			String role = "ROLE_SUPER_ADMIN";

			License license = licenseRepository.findByClientId(clientId);
			if (typeOfLicense.equalsIgnoreCase("custom")) {
				license.addActivityLog(Utils.setLogs("Request License: No of Concurrent Users-" + noOfUsers
						+ "; No of Well Manager-" + noOfWellManager + "; No of Well Engineer-" + noOfWellEngineer
						+ "; No of Other User-" + noOfOtherUser));
			} else {
				license.addActivityLog(Utils.setLogs("Request License: No of Concurrent Users-" + noOfUsers));
			}
			licenseRepository.save(license);
			List<Login> user1 = userRepository.findByRole(role);
			Iterator<Login> itr = user1.iterator();
			while (itr.hasNext()) {
				Login login = itr.next();
				String adminEmail = login.getUserId();
				if (adminEmail != null) {
					String body = "<h3>Hi SuperAdmin,</h3>" + "<p>" + user.getFirstName() + " " + user.getLastName()
							+ " " + " has requested:<br> number of Active Users: " + noOfUsers
							+ "<br> number of Well Manager: " + noOfWellManager + "<br> number of Well Engineer: "
							+ noOfWellEngineer + "<br> number of Well Other Users: " + noOfOtherUser + " </p>"
							+ "<a href=\"" + context + "\">click here</a>"
							+ "<p>This is an System generated message, Please don't reply to this email.</p>"
							+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
					UserServiceImpl userService = new UserServiceImpl();
					String message = "IwellBenchmark Notification: Requesting no of wells";
					userService.singleEmail(body, adminEmail, message);
					return "Success:Available";
				} else {
					return "Error:Not available";
				}

			}
		}
		return "error";
	}

	@RequestMapping(value = "/updateLicense", produces = "application/json")
	public String updateLicense(@RequestParam("clientId") String clientId, @RequestParam("noOfWells") String noOfWells,
			@RequestParam("noOfOldWells") int noOfOldWells, @RequestParam("startDate") String startDate,
			@RequestParam("noOfUsers") String noOfUsers, @RequestParam("customLicenseData") String customLicenseData,
			@RequestParam("isFirst") boolean isFirst) {

		License license = licenseRepository.findByClientId(clientId);
		license.setLastUpdatedDate(new Date());
		license.setNoOfWells(Integer.parseInt(noOfWells));
		license.setFirst(isFirst);
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		try {
			license.setStartDate(df.parse(startDate));
		} catch (Exception e) {
		}
		// license type
		if (customLicenseData != "") {
			JSONObject obj;
			try {
				obj = new JSONObject(customLicenseData);
				UserLicense userLicense;
				System.out.println(license.getUserLicenses().getLicenseType());
				System.out.println(obj.getString("licenseType"));
				System.out.println((license.getUserLicenses().getLicenseType()).equals(obj.getString("licenseType")));
				if ((license.getUserLicenses().getLicenseType()).equals(obj.getString("licenseType"))) {
					userLicense = userLicenseRepository.findById(license.getUserLicenses().getId());
				} else {
					userLicense = new UserLicense();
				}

				userLicense.setNoOfUsers(Integer.parseInt(noOfUsers));
				userLicense.setNoOfAdmin(obj.getInt("noOfAdmin"));
				userLicense.setNoOfWellManager(obj.getInt("noOfWellManager"));
				userLicense.setNoOfWellEngineer(obj.getInt("noOfWellEngineer"));
				userLicense.setNoOfOtherUser(obj.getInt("noOfOtherUser"));
				userLicense.setLicenseType(obj.getString("licenseType"));
				userLicenseRepository.save(userLicense);

				license.setUserLicenses(userLicense);
//				license.addActivityLog(Utils.setLogs("License updated: No of Wells-" + license.getNoOfWells() + "; No of Concurrent Users-"
//						+ license.getUserLicenses().getNoOfUsers()));
//				licenseRepository.save(license);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			license.setUserLicenses(userLicenseRepository.findByNoOfUsers(Integer.parseInt(noOfUsers)));
//			license.addActivityLog(Utils.setLogs("License updated: No of Wells-" + license.getNoOfWells() + "; No of Concurrent Users-"
//					+ license.getUserLicenses().getNoOfUsers()));
//			licenseRepository.save(license);
		}

		Client client = clientRepository.findById(clientId);
		Login login = userRepository.findByUserId(client.getUserId());
		if (login != null & client != null && license.isFirst() == true) {
			String message = "IwellsBenchmarking Notice: Registration Notice";
			String adminEmail = login.getUserId();
			if (adminEmail != null) {
				String adminbody = "<h3>Hi" + " " + login.getFirstName() + login.getLastName() + ",</h3>" + " "
						+ " <p>your License has been set for iwells successfully.please login and verify it.</p>";
				UserServiceImpl euserService = new UserServiceImpl();
				euserService.singleEmail(adminbody, adminEmail, message);
			}
			return "success:mail sended for initial license";

		} else if (login != null & client != null && license.isFirst() == false) {
			String message = "IwellsBenchmarking Notice: Registration Notice";
			String adminEmail = login.getUserId();

//			Old Well data update
			if (client.getNoOfOldWells() != noOfOldWells) {
				client.setNoOfOldWells(noOfOldWells);
				clientRepository.save(client);
				license.addActivityLog(Utils.setLogs("License updated: No of Old Wells-" + noOfOldWells
						+ ";No of New Wells-" + license.getNoOfWells() + "; No of Concurrent Users-"
						+ license.getUserLicenses().getNoOfUsers()));
				licenseRepository.save(license);
			} else {
				license.addActivityLog(Utils.setLogs("License updated: No of New Wells-" + license.getNoOfWells()
						+ "; No of Concurrent Users-" + license.getUserLicenses().getNoOfUsers()));
				licenseRepository.save(license);
			}

			if (adminEmail != null) {
				String adminbody = "<h3>Hi" + " " + login.getFirstName() + login.getLastName() + ",</h3>" + " "
						+ " <p>your License has been updated for iwells successfully.please login and verify it.</p>";
				UserServiceImpl euserService = new UserServiceImpl();
				euserService.singleEmail(adminbody, adminEmail, message);

				if (client.getRequestedUser() != "") {
					String message1 = "IwellsBenchmarking Notice: Registration Notice";
					String managerEmail = client.getRequestedUser();
					if (managerEmail != null) {
						String managerbody = "<h3>Hi" + " " + login.getFirstName() + login.getLastName() + ",</h3>"
								+ "<p>License updated</p><br>"
								+ ((client.getNoOfOldWells() != noOfOldWells)
										? "<p>No of Old Wells: " + noOfOldWells + "</p><br>"
										: "")
								+ "<p>No of New Wells: " + license.getNoOfWells() + "</p><br>"
								+ "<p>No of Concurrent Users: " + license.getUserLicenses().getNoOfUsers() + "</p><br>"
								+ "<p>Your License has been updated for iwells successfully.please login and verify it.</p>";
						UserServiceImpl muserService = new UserServiceImpl();
						muserService.singleEmail(managerbody, managerEmail, message1);
						client.setRequestedUser("");
						clientRepository.save(client);

					}
					return "success:updated mail sended to well manager";

				}

			}
			return "success:updated mail sended to admin";

		}
		return "Success:" + license.getClientId();

	}

	@RequestMapping(value = "/setMinimumLicense", produces = "application/json")
	public String updateLicense(@RequestParam("clientId") String clientId) {
		License license = licenseRepository.findByClientId(clientId);
		license.setNoOfWells(0);
		license.setLastUpdatedDate(new Date());
		license.setUserLicenses(userLicenseRepository.findByNoOfUsers(1));
		license.addActivityLog(Utils.setLogs("Minimum License updated: No of Wells-" + license.getNoOfWells()
				+ "; No of Users-" + license.getUserLicenses().getNoOfUsers()));
		licenseRepository.save(license);
		return "Success:Done";
	}

	@RequestMapping(value = "/getLicense", produces = "application/json")
	public String getLicense(@RequestParam("clientId") String clientId) {
		try {
			License license = licenseRepository.findByClientId(clientId);
			Client client = clientRepository.findById(clientId);
//			license_dataObj.put(, arg1)
//			license.licensed_noOfOldWell = client.getNoOfOldWells();
			JSONObject obj = new JSONObject(mapper.writeValueAsString(license));
			obj.put("licensed_noOfOldWell", client.getNoOfOldWells());

			return obj.toString();
		} catch (Exception e) {
			return "{}";
		}
	}

	@RequestMapping(value = "/canCreateWell", produces = "application/json")
	public String canCreateWell(@RequestParam("clientId") String clientId) {
		try {
			License license = licenseRepository.findByClientId(clientId);
			List<Well> wells = wellRepository.findByClientId(clientId);

			int cnt = 0;

			Iterator<Well> itr = wells.iterator();
			while (itr.hasNext()) {
				Well well = itr.next();
				if (well.getOldWell() != null && !well.getOldWell().equals("true")) {
					cnt++;
				}
			}

			if (license == null) {
				return "Error:Your License is not set.";
			} else {
				if (cnt == license.getNoOfWells()) {
					return "Error:Reached maximum no of wells. Please upgrade the license";
				} else {
					return "Success:Can create";
				}
			}
		} catch (Exception e) {
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/getOldWellCount", produces = "application/json")
	public String getOldWellCount(@RequestParam("clientId") String clientId) {
		try {
			JSONObject obj = new JSONObject();
			List<Well> wells = wellRepository.findByClientId(clientId);

			int cnt = 0;

			Iterator<Well> itr = wells.iterator();
			while (itr.hasNext()) {
				Well well = itr.next();
				if (well.getOldWell() != null && well.getOldWell().equals("true")) {
					cnt++;
				}
			}
			obj.put("usedOldWellCount", cnt);
			Client client = clientRepository.findById(clientId);
			obj.put("licensedOldWellCount", client.getNoOfOldWells());

			return obj.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	@RequestMapping(value = "/forgetPassword", produces = "application/json")
	public String forgetPassword(@RequestParam("userId") String userId, HttpServletRequest request,
			HttpServletResponse response) {

		Login login = userRepository.findByUserId(userId);
		String context = "";
		context = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		String message = "IwellsBenchmarking Notice: Password Reset";

		if (login != null) {
			String tokenId = UUID.randomUUID().toString();

			String body = "<p>You're receiving this email because you requested a password reset for your account.</p>"
					+ "<a href=\"" + context + "/resetPassword?userId=" + userId + "&resetToken=" + tokenId
					+ "\">Reset Password </a>";
			UserServiceImpl userService = new UserServiceImpl();
			userService.singleEmail(body, userId, message);

			ForgetPassword forgetPassword = new ForgetPassword();
			forgetPassword.setUserId(userId);
			forgetPassword.setFlag(true);
			forgetPassword.setForgetPasswordToken(tokenId);
			forgetPassword.setUpdatedDate(new Date());
			forgetPassword.setExpiryDate(new Date());
			forgetPasswordRepository.save(forgetPassword);

			return "Success:Available";
		} else {
			return "Error:Not available";
		}
	}

	@RequestMapping(value = "/resetPasswordSubmit", method = RequestMethod.POST, produces = "application/json")
	public String resetPasswordSubmit(@RequestParam("userId") String userId,
			@RequestParam("resetToken") String resetToken, @RequestParam("newPassword") String newPassword) {

		ForgetPassword forgetpassword = forgetPasswordRepository.findByForgetPasswordToken(resetToken);
		forgetpassword.setFlag(false);
		forgetPasswordRepository.save(forgetpassword);

		Login login = userRepository.findByUserId(userId);
		login.setPassword(bCryptPasswordEncoder.encode(newPassword));
		userRepository.save(login);
		return "index";

	}

	@RequestMapping(value = "/lockWellInput", method = RequestMethod.POST, produces = "application/json")
	public String lockWellInput(@RequestParam("wellId") String wellId, @RequestParam("userId") String userId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				well.setLastAccessedUser(userId);
				well.setLocked(true);
				well.setLastUpdatedTime(new Date());
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error: error";
		}
	}

	@RequestMapping(value = "/pingWellInput", produces = "application/json")
	public String pingWellInput(@RequestParam("wellId") String wellId, @RequestParam("userId") String userId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				well.setLastAccessedUser(userId);
				well.setLocked(true);
				well.setLastUpdatedTime(new Date());
				wellRepository.save(well);
				return "Success: done";
			} else {
				return "Error: not founnd";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/unlockWellInput", method = RequestMethod.POST, produces = "application/json")
	public String unlockWellInput(@RequestParam("wellId") String wellId, @RequestParam("userId") String userId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null && well.getLastAccessedUser().equals(userId)) {
				well.setLocked(false);
				// well.setLastAccessedUser("");
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/archiveWellInput", method = RequestMethod.POST, produces = "application/json")
	public String archiveWellInput(@RequestParam("wellId") String wellId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				well.setWellInputArchived(true);
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/unArchiveWellInput", method = RequestMethod.POST, produces = "application/json")
	public String unArchiveWellInput(@RequestParam("wellId") String wellId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				well.setWellInputArchived(false);
				wellRepository.save(well);
				return "Success:Done";
			} else {
				return "Error:Not found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/canAccessWell", method = RequestMethod.POST, produces = "application/json")
	public String canAccessWell(@RequestParam("wellId") String wellId, @RequestParam("userId") String userId) {
		try {
			Well well = wellRepository.findById(wellId);
			if (well != null) {
				if (well.getLastUpdatedTime() != null) {
					Date curDate = new Date();
					long seconds = (curDate.getTime() - well.getLastUpdatedTime().getTime()) / 1000;
					if (well.isLocked())
						if (seconds <= 180 && well.getLastAccessedUser().equals(userId))
							return "true";
						else if (seconds <= 180)
							return "false";
						else
							return "true";
					else
						return "true";

				}
				return well.isLocked() ? "false" : "true";
			} else {
				return "Error:Not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}
	}

	@RequestMapping(value = "/retrieveWellUnits", method = RequestMethod.GET, produces = "application/json")
	public String retrieveWellUnits(@RequestParam("masterApi") String masterApi) {
		WellUnit unit = UnitRepository.findByMasterApi(masterApi);
		// WellUnit wellunit=(WellUnit)unit.get(length);
		if (unit != null) {
			JSONArray arr = new JSONArray();
			JSONObject obj1 = new JSONObject();
			try {
				obj1.put("depth", unit.getDepth());
				obj1.put("diameter", unit.getDiameter());
				obj1.put("displacement", unit.getDisplacement());
				obj1.put("drillingRate", unit.getDrillingRate());
				obj1.put("flowRate", unit.getFlowRate());
				obj1.put("generalWeights", unit.getGeneralWeights());
				obj1.put("jetVelocity", unit.getJetVelocity());
				obj1.put("mudWeight", unit.getMudWeight());
				obj1.put("pressure", unit.getPressure());
				obj1.put("pumpOutput", unit.getPumpOutput());
				obj1.put("rihAndPoohSpeed", unit.getRihAndPoohSpeed());
				obj1.put("rihAndPoohTime", unit.getRihAndPoohTime());
				obj1.put("stringWeight", unit.getStringWeight());
				obj1.put("torque", unit.getTorque());
				obj1.put("velocity", unit.getVelocity());
				obj1.put("volume", unit.getVolume());
			} catch (JSONException e) {
				e.printStackTrace();
				return "[]";
			}
			arr.put(obj1);
			return arr.toString();

		} else {
			return "[]";
		}
	}

	@RequestMapping(value = "/retrieveMixedUnits", method = RequestMethod.GET, produces = "application/json")
	public String retrieveMixedUnits(@RequestParam("wellId") String wellId) {
		WellUnit unit = UnitRepository.findByWellId(wellId);
		// WellUnit wellunit=(WellUnit)unit.get(length);
		if (unit != null) {
			JSONArray arr = new JSONArray();
			JSONObject obj1 = new JSONObject();
			obj1.put("depth", unit.getDepth());
			obj1.put("diameter", unit.getDiameter());
			obj1.put("displacement", unit.getDisplacement());
			obj1.put("drillingRate", unit.getDrillingRate());
			obj1.put("flowRate", unit.getFlowRate());
			obj1.putOnce("generalWeights", unit.getGeneralWeights());
			obj1.put("jetVelocity", unit.getJetVelocity());
			obj1.put("mudWeight", unit.getMudWeight());
			obj1.put("pressure", unit.getPressure());
			obj1.put("pumpOutput", unit.getPumpOutput());
			obj1.put("rihAndPoohSpeed", unit.getRihAndPoohSpeed());
			obj1.put("rihAndPoohTime", unit.getRihAndPoohTime());
			obj1.put("stringWeight", unit.getStringWeight());
			obj1.put("torque", unit.getTorque());
			obj1.put("velocity", unit.getVelocity());
			obj1.put("volume", unit.getVolume());
			arr.put(obj1);
			// String size=Integer.toString(unit.size());

			return arr.toString();

		} else {
			return "error:not found";
		}
	}

	@RequestMapping(value = "/retrieveUnits", method = RequestMethod.GET, produces = "application/json")
	public String retrieveUnits(@RequestParam("wellId") String wellId) {
		WellUnit unit = UnitRepository.findByWellId(wellId);
		// WellUnit wellunit=(WellUnit)unit.get(length);
		if (unit != null && unit.isSubmitted() == true) {

			return unit.getUnitSelect();
		}

		else {
			return "false";
		}

	}

	@RequestMapping(value = "/submitmixedWellUnits", method = RequestMethod.POST, produces = "application/json")
	public String saveWellUnits(@RequestParam("masterApi") String masterApi,
			@RequestParam("unitSelect") String unitSelect, @RequestParam("depth") String depth,
			@RequestParam("diameter") String diameter, @RequestParam("mudWeight") String mudWeight,
			@RequestParam("volume") String volume, @RequestParam("drillingRate") String drillingRate,
			@RequestParam("generalWeights") String generalWeights, @RequestParam("stringWeight") String stringWeight,
			@RequestParam("torque") String torque, @RequestParam("pressure") String pressure,
			@RequestParam("flowRate") String flowRate, @RequestParam("pumpOutput") String pumpOutput,
			@RequestParam("displacement") String displacement, @RequestParam("jetVelocity") String jetVelocity,
			@RequestParam("velocity") String velocity, @RequestParam("rihAndPoohSpeed") String rihAndPoohSpeed,
			@RequestParam("rihAndPoohTime") String rihAndPoohTime) {
		try {
			WellUnit units = UnitRepository.findByMasterApi(masterApi);
			// Login login=userRepository.findById(id);
			if (units != null) {
				units.setDepth(depth);
				units.setMasterApi(masterApi);
				units.setDiameter(diameter);
				units.setDisplacement(displacement);
				units.setDrillingRate(drillingRate);
				units.setFlowRate(flowRate);
				units.setGeneralWeights(generalWeights);
				units.setJetVelocity(jetVelocity);
				units.setMudWeight(mudWeight);
				units.setStringWeight(stringWeight);
				units.setPressure(pressure);
				units.setPumpOutput(pumpOutput);
				units.setRihAndPoohSpeed(rihAndPoohSpeed);
				units.setRihAndPoohTime(rihAndPoohTime);
				units.setVelocity(velocity);
				units.setVolume(volume);
				units.setSubmitted(true);
				units.setTorque(torque);
				units.setUnitSelect(unitSelect);
				// unit.setDrillingRate(wellunit.get);
				// int wellUnitAccess=login.getWellUnitAccess()+1;

				// login.setWellUnitAccess(wellUnitAccess);
				// userRepository.save(login);
				UnitRepository.save(units);

				return "Success:done";
			} else {
				WellUnit unit = new WellUnit();
				unit.setMasterApi(masterApi);
				unit.setDepth(depth);
				unit.setDiameter(diameter);
				unit.setDisplacement(displacement);
				unit.setDrillingRate(drillingRate);
				unit.setFlowRate(flowRate);
				unit.setGeneralWeights(generalWeights);
				unit.setJetVelocity(jetVelocity);
				unit.setMudWeight(mudWeight);
				unit.setStringWeight(stringWeight);
				unit.setPressure(pressure);
				unit.setPumpOutput(pumpOutput);
				unit.setRihAndPoohSpeed(rihAndPoohSpeed);
				unit.setRihAndPoohTime(rihAndPoohTime);
				unit.setVelocity(velocity);
				unit.setVolume(volume);
				unit.setTorque(torque);
				unit.setSubmitted(true);
				unit.setUnitSelect(unitSelect);
				// unt.setDrillingRate(wellunit.get);
				// int wellUnitAccess=login.getWellUnitAccess()+1;

				// login.setWellUnitAccess(wellUnitAccess);
				// userRepository.save(login);
				UnitRepository.save(unit);

				return "Success:done";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error: error";
		}

	}

	@RequestMapping(value = "/submitWellUnits", method = RequestMethod.POST, produces = "application/json")
	public String submitWellUnits(@RequestParam("wellId") String wellId, @RequestParam("unitSelect") String unitSelect,
			@RequestParam("depth") String depth, @RequestParam("diameter") String diameter,
			@RequestParam("mudWeight") String mudWeight, @RequestParam("volume") String volume,
			@RequestParam("drillingRate") String drillingRate, @RequestParam("generalWeights") String generalWeights,
			@RequestParam("stringWeight") String stringWeight, @RequestParam("torque") String torque,
			@RequestParam("pressure") String pressure, @RequestParam("flowRate") String flowRate,
			@RequestParam("pumpOutput") String pumpOutput, @RequestParam("displacement") String displacement,
			@RequestParam("jetVelocity") String jetVelocity, @RequestParam("velocity") String velocity,
			@RequestParam("rihAndPoohSpeed") String rihAndPoohSpeed,
			@RequestParam("rihAndPoohTime") String rihAndPoohTime) {
		try {
			WellUnit units = UnitRepository.findByWellId(wellId);
			// Login login=userRepository.findById(id);
			if (units != null) {
				units.setWellId(wellId);
				units.setDepth(depth);
				units.setDiameter(diameter);
				units.setDisplacement(displacement);
				units.setDrillingRate(drillingRate);
				units.setFlowRate(flowRate);
				units.setGeneralWeights(generalWeights);
				units.setJetVelocity(jetVelocity);
				units.setMudWeight(mudWeight);
				units.setStringWeight(stringWeight);
				units.setPressure(pressure);
				units.setPumpOutput(pumpOutput);
				units.setRihAndPoohSpeed(rihAndPoohSpeed);
				units.setRihAndPoohTime(rihAndPoohTime);
				units.setVelocity(velocity);
				units.setVolume(volume);
				units.setTorque(torque);
				units.setUnitSelect(unitSelect);
				units.setSubmitted(true);
				// unit.setDrillingRate(wellunit.get);
				// int wellUnitAccess=login.getWellUnitAccess()+1;

				// login.setWellUnitAccess(wellUnitAccess);
				// userRepository.save(login);
				UnitRepository.save(units);

				return "Success:done";
			} else {
				WellUnit unit = new WellUnit();
				unit.setWellId(wellId);
				unit.setDepth(depth);
				unit.setDiameter(diameter);
				unit.setDisplacement(displacement);
				unit.setDrillingRate(drillingRate);
				unit.setFlowRate(flowRate);
				unit.setGeneralWeights(generalWeights);
				unit.setJetVelocity(jetVelocity);
				unit.setMudWeight(mudWeight);
				unit.setStringWeight(stringWeight);
				unit.setPressure(pressure);
				unit.setPumpOutput(pumpOutput);
				unit.setRihAndPoohSpeed(rihAndPoohSpeed);
				unit.setRihAndPoohTime(rihAndPoohTime);
				unit.setVelocity(velocity);
				unit.setVolume(volume);
				unit.setSubmitted(true);
				unit.setTorque(torque);
				unit.setUnitSelect(unitSelect);
				// unt.setDrillingRate(wellunit.get);
				// int wellUnitAccess=login.getWellUnitAccess()+1;

				// login.setWellUnitAccess(wellUnitAccess);
				// userRepository.save(login);
				UnitRepository.save(unit);

				return "Success:submitted";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error:Error";
		}

	}

	@RequestMapping(value = "/requestedWell", method = RequestMethod.POST, produces = "application/json")
	public String RequestedWell(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId,
			@RequestParam("noOfWells") int noOfWells, HttpServletRequest request, HttpServletResponse response) {
		Login user = userRepository.findByUserId(userId);
		String context = "";
		context = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		if (user != null && user.getRole().equals("ROLE_WELL_MANAGER")) {
			System.out.println("ROLE_WELL_MANAGER");
			String tokenId = UUID.randomUUID().toString();
			String body = "<h3>Hi Admin,</h3>" + "<p>" + user.getFirstName() + " " + user.getLastName() + " "
					+ " has requested " + noOfWells + " number of wells</p>" + "<a href=\"" + context
					+ "/wellRequest?&noOfwells=" + noOfWells + "&userId=" + user.getUserId() + "&firstName="
					+ user.getFirstName() + "&lastName=" + user.getLastName() + "&clientId=" + clientId
					+ "&wellType=newwell&tokenId=" + tokenId + "\">click here</a>"
					+ "<p>This is an System generated message, Please don't reply to this email.</p>"
					+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
			UserServiceImpl userService = new UserServiceImpl();
			String message = "IwellsBenchmarking Notice:  Requested no of wells";
			userService.singleEmail(body, user.getReportId(), message);
			WellRequest requestWell = new WellRequest();
			requestWell.setClientId(clientId);
			requestWell.setFlag(true);
			requestWell.setUserId(userId);
			requestWell.setTokenId(tokenId);
			wellRequestRepository.save(requestWell);
			return "Success:Available";
		} else if (user != null && user.getRole().equals("ROLE_ADMIN")) {
			String role = "ROLE_SUPER_ADMIN";
			System.out.println("ROLE_ADMIN");

			License license = licenseRepository.findByClientId(clientId);
			license.addActivityLog(Utils.setLogs("Request License: No of Wells-" + noOfWells));
			licenseRepository.save(license);

			List<Login> user1 = userRepository.findByRole(role);
			Iterator<Login> itr = user1.iterator();
			while (itr.hasNext()) {
				Login login = itr.next();
				String adminEmail = login.getUserId();
				if (adminEmail != null) {
					String body = "<h3>Hi SuperAdmin,</h3>" + "<p>" + user.getFirstName() + " " + user.getLastName()
							+ " " + " has requested " + noOfWells + " number of wells</p>" + "<a href=\"" + context
							+ "\">click here</a>"
							+ "<p>This is an System generated message, Please don't reply to this email.</p>"
							+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
					UserServiceImpl userService = new UserServiceImpl();
					String message = "IwellBenchmark Notification: Requesting no of wells";
					userService.singleEmail(body, adminEmail, message);
					return "Success:Available";
				} else {
					return "Error:Not available";
				}

			}
		}
		return "error";
	}

	@RequestMapping(value = "/requestedOldWell", method = RequestMethod.POST, produces = "application/json")
	public String RequestedOldWell(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId,
			@RequestParam("noOfWells") int noOfWells, HttpServletRequest request, HttpServletResponse response) {
		Login user = userRepository.findByUserId(userId);
		String context = "";
		context = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		if (user != null && user.getRole().equals("ROLE_WELL_MANAGER")) {
			System.out.println("ROLE_WELL_MANAGER");
			String tokenId = UUID.randomUUID().toString();
			String body = "<h3>Hi Admin,</h3>" + "<p>" + user.getFirstName() + " " + user.getLastName() + " "
					+ " has requested " + noOfWells + " number of Old Wells</p>" + "<a href=\"" + context
					+ "/wellRequest?&noOfwells=" + noOfWells + "&userId=" + user.getUserId() + "&firstName="
					+ user.getFirstName() + "&lastName=" + user.getLastName() + "&clientId=" + clientId
					+ "&wellType=oldwell&tokenId=" + tokenId + "\">click here</a>"
					+ "<p>This is an System generated message, Please don't reply to this email.</p>"
					+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
			UserServiceImpl userService = new UserServiceImpl();
			String message = "IwellsBenchmarking Notice:  Requested no of Old Wells";
			userService.singleEmail(body, user.getReportId(), message);
			WellRequest requestWell = new WellRequest();
			requestWell.setClientId(clientId);
			requestWell.setFlag(true);
			requestWell.setUserId(userId);
			requestWell.setTokenId(tokenId);
			wellRequestRepository.save(requestWell);
			return "Success:Available";
		} else if (user != null && user.getRole().equals("ROLE_ADMIN")) {
			String role = "ROLE_SUPER_ADMIN";
			System.out.println("ROLE_ADMIN");

			License license = licenseRepository.findByClientId(clientId);
			license.addActivityLog(Utils.setLogs("Request License: No of Old Wells-" + noOfWells));
			licenseRepository.save(license);

			List<Login> user1 = userRepository.findByRole(role);
			Iterator<Login> itr = user1.iterator();
			while (itr.hasNext()) {
				Login login = itr.next();
				String adminEmail = login.getUserId();
				if (adminEmail != null) {
					String body = "<h3>Hi SuperAdmin,</h3>" + "<p>" + user.getFirstName() + " " + user.getLastName()
							+ " " + " has requested " + noOfWells + " number of Old Wells</p>" + "<a href=\"" + context
							+ "\">click here</a>"
							+ "<p>This is an System generated message, Please don't reply to this email.</p>"
							+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
					UserServiceImpl userService = new UserServiceImpl();
					String message = "IwellBenchmark Notification: Requesting no of Old Wells";
					userService.singleEmail(body, adminEmail, message);
					return "Success:Available";
				} else {
					return "Error:Not available";
				}

			}
		}
		return "error";
	}

	@RequestMapping(value = "/updateRequestedWell", method = RequestMethod.POST, produces = "application/json")
	public String updateRequestedWell(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId,
			@RequestParam("tokenId") String tokenId, @RequestParam("noOfWells") int noOfWells,
			HttpServletRequest request, HttpServletResponse response) {

		Client client = clientRepository.findById(clientId);

		Login manager = userRepository.findByUserId(userId);

		if (client != null && manager != null) {

			String body = "<h3>Hi, SuperAdmin</h3>" + "<p>" + manager.getFirstName() + " " + manager.getLastName() + " "
					+ " has requested  " + noOfWells + " " + " number of wells.</p>"
					+ "<p>This is an System generated message, Please don't reply to this email.</p>"
					+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";

			UserServiceImpl userService = new UserServiceImpl();
			String message = "IwellsBenchmarking Notice:  Requested no of wells";
			String role = "ROLE_SUPER_ADMIN";
			System.out.println("ROLE_ADMIN");

			List<Login> user1 = userRepository.findByRole(role);
			Iterator<Login> itr = user1.iterator();
			while (itr.hasNext()) {
				Login login = itr.next();
				String adminEmail = login.getUserId();
				if (adminEmail != null) {
					userService.singleEmail(body, adminEmail, message);
					client.setRequestedUser(manager.getUserId());
					clientRepository.save(client);

					WellRequest requestWell = wellRequestRepository.findByTokenId(tokenId);
					if (requestWell != null) {
						requestWell.setClientId(clientId);
						requestWell.setFlag(false);
						requestWell.setUserId(userId);
						requestWell.setTokenId(tokenId);
						wellRequestRepository.save(requestWell);
					}

				}
			}
			return "Success:Mail sended successfully";

		} else {
			return "error:link has been expired";
		}

	}

	@RequestMapping(value = "/cancelRequestedWell", method = RequestMethod.POST, produces = "application/json")
	public String cancelRequestedWell(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId,
			@RequestParam("tokenId") String tokenId, @RequestParam("noOfWells") int noOfWells,
			HttpServletRequest request, HttpServletResponse response) {

		Client client = clientRepository.findById(clientId);
		Login manager = userRepository.findByUserId(userId);

		if (client != null && manager != null) {
			String message1 = "IwellsBenchmarking Notice: Request Cancellation";
			String managerEmail = client.getRequestedUser();
			if (managerEmail != null) {
				String managerbody = "<h3>Hi" + " " + manager.getFirstName() + manager.getLastName() + ",</h3>" + " "
						+ " <p>your License to create new well has been cancelled by admin.</p>";
				UserServiceImpl muserService = new UserServiceImpl();
				muserService.singleEmail(managerbody, managerEmail, message1);
				WellRequest requestWell = wellRequestRepository.findByTokenId(tokenId);
				if (requestWell != null) {
					requestWell.setClientId(clientId);
					requestWell.setFlag(false);
					requestWell.setUserId(userId);
					requestWell.setTokenId(tokenId);
					wellRequestRepository.save(requestWell);
				}

			}
			return "Success:Mail sended successfully";

		} else {
			return "error:user not found.";
		}
	}
	@RequestMapping(value = "/sendPdf", method = RequestMethod.POST,consumes = "multipart/form-data", produces = "application/json")
	public String sendPdf(@RequestParam("upload") MultipartFile file,@RequestParam("mailId") String mailId) {
		try {
			UserServiceImpl userImpl = new UserServiceImpl();
			String body = "<h3>Hi,</h3>" + "<p>Please see the attached PDF report for AFE Estimation.</p>"
			+ "<p>This is an System generated message, Please don't reply to this email.</p>"
			+ "<p> Thanks,</p> " + "<p> Iwell Team.</p>";
	String message = "IwellBenchmark Notification: Requesting no of wells";
			userImpl.singleEmailPdf(body, mailId, message, file);
			return "Success:Done";
		}
		catch(Exception e){
			e.printStackTrace();
			return "Failed";
		}
		
		
	}
	
}
