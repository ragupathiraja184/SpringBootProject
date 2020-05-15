
package com.zeptoh.benchmarking.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeptoh.benchmarking.model.ActivityLog;
import com.zeptoh.benchmarking.model.WellInputLevel;
import com.zeptoh.benchmarking.model.WellInputLevel2Node;
import com.zeptoh.benchmarking.model.WellInputLevel3Node;
import com.zeptoh.benchmarking.model.WellInputLevel4Node;
import com.zeptoh.benchmarking.model.WellInputLevel5Node;
import com.zeptoh.benchmarking.model.WellInputPhase;
import com.zeptoh.benchmarking.model.WellParameters;

public class Utils {

	public static int randomNumber() {
		Random r = new Random();
		int Low = 175;
		int High = 285;
		return r.nextInt(High - Low) + Low;
	}

	public static ActivityLog setLogs(String msg) {
		ActivityLog aL = new ActivityLog();
		Date date = new Date();
		aL.setDate(date);
		aL.setMsg(msg);
		return aL;
	}

	public static String convertWellInputObjToCSV_1(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			System.out.println(wellInputJson);
			System.out.println("obj length : " + obj.length());
			csv.append(
					"levelValue,Phase Name,MD,TVD,Inclination,Activity Category,Off Bottom,On Bottom,Date,Time From,Time To,Hours taken for operation\n");
			// csv.insert(0, "MD,TVD,Inclination,Activity Category,Off Bottom,On
			// Bottom,Date,Time From,Time To,Hours taken for operation\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				// System.out.println("pObj : " + pObj);
				if (pObj.has("phaseName")) {
					csv.append(pObj.getInt("levelValue") + "," + pObj.getString("phaseName") + ",");
					if (pObj.has("md") || pObj.has("tvd") || pObj.has("inclination") || pObj.has("activity_Category")
							|| pObj.has("date") || pObj.has("time_From") || pObj.has("time_To")
							|| pObj.has("hours_taken_for_operation")) {
						csv.append(pObj.getString("md") + ",");
						csv.append(pObj.getString("tvd") + ",");
						csv.append(pObj.getString("inclination") + ",");
						csv.append(pObj.getString("activity_Category") + ",");
						if (pObj.getString("activity_Category").equals("Off Bottom")) {
							csv.append(pObj.getString("off_Bottom") + ",,");
						} else {
							csv.append("," + pObj.getString("on_Bottom") + ",");
						}
						csv.append(pObj.getString("date") + ",");
						csv.append(pObj.getString("time_From") + ",");
						csv.append(pObj.getString("time_To") + ",");
						csv.append(pObj.getString("hours_taken_for_operation") + "\n");
					} else {
						csv.append(",,,,,,,,,,,,,,,,,,,,,,,\n");
					}
				}

			}
			return csv.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV_2(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			csv.append(
					"levelValue,Phase Name,Level 2,MD,TVD,Inclination,Activity Category,Off Bottom,On Bottom,Date,Time From,Time To,Hours taken for operation\n");

			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phaseName")) {
					csv.append(pObj.getInt("levelValue") + "," + pObj.getString("phaseName")
							+ ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("subLevels")) {
						JSONArray l1Ar = (JSONArray) pObj.get("subLevels");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							csv.append(l1Obj.getInt("levelValue") + ",," + l1Obj.getString("phaseName") + ",");
							if (l1Obj.has("md") || l1Obj.has("tvd") || l1Obj.has("inclination")
									|| l1Obj.has("activity_Category") || l1Obj.has("date") || l1Obj.has("time_From")
									|| l1Obj.has("time_To") || l1Obj.has("hours_taken_for_operation")) {
								csv.append(l1Obj.getString("md") + ",");
								csv.append(l1Obj.getString("tvd") + ",");
								csv.append(l1Obj.getString("inclination") + ",");
								csv.append(l1Obj.getString("activity_Category") + ",");
								if (l1Obj.getString("activity_Category").equals("Off Bottom")) {
									csv.append(l1Obj.getString("off_Bottom") + ",,");
								} else {
									csv.append("," + l1Obj.getString("on_Bottom") + ",");
								}
								csv.append(l1Obj.getString("date") + ",");
								csv.append(l1Obj.getString("time_From") + ",");
								csv.append(l1Obj.getString("time_To") + ",");
								csv.append(l1Obj.getString("hours_taken_for_operation") + "\n");
							} else {
								csv.append(",,,,,,,,,,,,,,,,,,,,,,,\n");
							}
						}

					}
				}
			}
			return csv.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV_3(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			// System.out.println("test length are::"+obj.get(0).toString().contains("MD"));

			csv.append(
					"levelValue,Phase Name,Level 2,Level 3,MD,TVD,Inclination,Activity Category,Off Bottom,On Bottom,Date,Time From,Time To,Hours taken for operation");
			csv.append(String.format("%n", ""));
			// System.out.println("test1" + csv.indexOf("\n"));

			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				// System.out.println("test1" + pObj.has("phaseName"));
				if (pObj.has("phaseName")) {
					csv.append(
							pObj.getInt("levelValue") + "," + pObj.getString("phaseName") + ",,,,,,,,,,,,,,,,,,,,,,,");
					csv.append(String.format("%n", ""));
					if (pObj.has("subLevels")) {
						JSONArray l1Ar = (JSONArray) pObj.get("subLevels");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("subLevels")) {
								csv.append(l1Obj.getInt("levelValue") + ",," + l1Obj.getString("phaseName") + ","
										+ ",,,,,,,,,,,,,,,,,,,,,,");
								csv.append(String.format("%n", ""));
							}
							if (l1Obj.has("subLevels") && !l1Obj.get("subLevels").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("subLevels");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									csv.append(l3Obj.getInt("levelValue") + ",,," + l3Obj.getString("phaseName") + ",");
									if (l3Obj.has("md") || l3Obj.has("tvd") || l3Obj.has("inclination")
											|| l3Obj.has("activity_Category") || l3Obj.has("date")
											|| l3Obj.has("time_From") || l3Obj.has("time_To")
											|| l3Obj.has("hours_taken_for_operation")) {
										csv.append(l3Obj.getString("md") + ",");
										csv.append(l3Obj.getString("tvd") + ",");
										csv.append(l3Obj.getString("inclination") + ",");
										csv.append(l3Obj.getString("activity_Category") + ",");
										if (l3Obj.getString("activity_Category").equals("Off Bottom")) {
											csv.append(l3Obj.getString("off_Bottom") + ",,");
										} else {
											csv.append("," + l3Obj.getString("on_Bottom") + ",");
										}
										csv.append(l3Obj.getString("date") + ",");
										csv.append(l3Obj.getString("time_From") + ",");
										csv.append(l3Obj.getString("time_To") + ",");
										csv.append(l3Obj.getString("hours_taken_for_operation"));
										csv.append(String.format("%n", ""));
									} else {
										csv.append(",,,,,,,,,,,,,,,,,,");
										csv.append(String.format("%n", ""));
									}
								}
							}
						}
					}
				}
			}
			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV_4(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			// System.out.println("json are" + wellInputJson);
			csv.append(
					"levelValue,Phase Name,Level 2,Level 3,Level 4,MD,TVD,Inclination,Activity Category,Off Bottom,On Bottom,Date,Time From,Time To,Hours taken for operation\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phaseName")) {
					csv.append(pObj.getInt("levelValue") + "," + pObj.getString("phaseName")
							+ ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("subLevels")) {
						JSONArray l1Ar = (JSONArray) pObj.get("subLevels");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("subLevels")) {
								csv.append(l1Obj.getInt("levelValue") + ",," + l1Obj.getString("phaseName")
										+ ",,,,,,,,,,,,,,,,,,,,,,\n");
							}
							if (l1Obj.has("subLevels") && !l1Obj.get("subLevels").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("subLevels");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									if (l3Obj.has("subLevels")) {
										csv.append(l3Obj.getInt("levelValue") + ",,," + l3Obj.getString("phaseName")
												+ ",,,,,,,,,,,,,,,,,,,,,,\n");
									}
									if (l3Obj.has("subLevels")) {
										JSONArray l4Ar = (JSONArray) l3Obj.get("subLevels");
										for (int l = 0; l < l4Ar.length(); l++) {
											JSONObject l4Obj = (JSONObject) l4Ar.get(l);
											if (l4Obj.has("subLevels")) {
												csv.append(l4Obj.getInt("levelValue") + ",,,,"
														+ l4Obj.getString("phaseName") + ",");
												if (l4Obj.has("md") || l4Obj.has("tvd") || l4Obj.has("inclination")
														|| l4Obj.has("activity_Category") || l4Obj.has("date")
														|| l4Obj.has("time_From") || l4Obj.has("time_To")
														|| l4Obj.has("hours_taken_for_operation")) {
													csv.append(l4Obj.getString("md") + ",");
													csv.append(l4Obj.getString("tvd") + ",");
													csv.append(l4Obj.getString("inclination") + ",");
													csv.append(l4Obj.getString("activity_Category") + ",");
													if (l4Obj.getString("activity_Category").equals("Off Bottom")) {
														csv.append(l4Obj.getString("off_Bottom") + ",,");
													} else {
														csv.append("," + l4Obj.getString("on_Bottom") + ",");
													}
													csv.append(l4Obj.getString("date") + ",");
													csv.append(l4Obj.getString("time_From") + ",");
													csv.append(l4Obj.getString("time_To") + ",");
													csv.append(l4Obj.getString("hours_taken_for_operation") + "\n");
												} else {
													csv.append(",,,,,,,,,,,,,,,,,,,,,,,\n");
												}
											}

										}

									}
								}
							}
						}
					}
				}
			}
			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV_5(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);

			csv.append(
					"levelValue,Phase Name,Level 2,Level 3,Level 4,Level 5,MD,TVD,Inclination,Activity Category,Off Bottom,On Bottom,Date,Time From,Time To,Hours taken for operation\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phaseName")) {
					csv.append(pObj.getInt("levelValue") + "," + pObj.getString("phaseName")
							+ ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("subLevels")) {
						JSONArray l1Ar = (JSONArray) pObj.get("subLevels");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("subLevels")) {
								csv.append(l1Obj.getInt("levelValue") + ",," + l1Obj.getString("phaseName")
										+ ",,,,,,,,,,,,,,,,,,,,,,\n");
							}
							if (l1Obj.has("subLevels") && !l1Obj.get("subLevels").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("subLevels");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									if (l3Obj.has("subLevels")) {
										csv.append(l3Obj.getInt("levelValue") + ",,," + l3Obj.getString("phaseName")
												+ ",,,,,,,,,,,,,,,,,,,,,,\n");
									}
									if (l3Obj.has("subLevels")) {
										JSONArray l4Ar = (JSONArray) l3Obj.get("subLevels");
										for (int l = 0; l < l4Ar.length(); l++) {
											JSONObject l4Obj = (JSONObject) l4Ar.get(l);
											if (l4Obj.has("subLevels")) {
												csv.append(l4Obj.getInt("levelValue") + ",,,,"
														+ l4Obj.getString("phaseName") + ",,,,,,,,,,,,,,,,,,,,,,\n");
											}
											if (l4Obj.has("subLevels")) {
												JSONArray l5Ar = (JSONArray) l4Obj.get("subLevels");
												for (int m = 0; m < l5Ar.length(); m++) {
													JSONObject l5Obj = (JSONObject) l5Ar.get(m);
													if (l5Obj.has("subLevels")) {
														csv.append(l5Obj.getInt("levelValue") + ",,,,,"
																+ l5Obj.getString("phaseName") + ",");
														if (l5Obj.has("md") || l5Obj.has("tvd")
																|| l5Obj.has("inclination")
																|| l5Obj.has("activity_Category") || l5Obj.has("date")
																|| l5Obj.has("time_From") || l5Obj.has("time_To")
																|| l5Obj.has("hours_taken_for_operation")) {
															csv.append(l5Obj.getString("md") + ",");
															csv.append(l5Obj.getString("tvd") + ",");
															csv.append(l5Obj.getString("inclination") + ",");
															csv.append(l5Obj.getString("activity_Category") + ",");
															if (l5Obj.getString("activity_Category")
																	.equals("Off Bottom")) {
																csv.append(l5Obj.getString("off_Bottom") + ",,");
															} else {
																csv.append("," + l5Obj.getString("on_Bottom") + ",");
															}
															csv.append(l5Obj.getString("date") + ",");
															csv.append(l5Obj.getString("time_From") + ",");
															csv.append(l5Obj.getString("time_To") + ",");
															csv.append(l5Obj.getString("hours_taken_for_operation")
																	+ "\n");
														} else {
															csv.append(",,,,,,,,,,,,,,,,,,,,,,,\n");
														}

													}
												}

											}
										}

									}
								}
							}
						}
					}
				}
			}

			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV1(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			System.out.println(wellInputJson);
			System.out.println("obj length : " + obj.length());
			csv.append("S.No,Phase Name\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				System.out.println("pObj : " + pObj);
				if (pObj.has("phase_Name")) {
					csv.append(pObj.getString("s_No") + "," + pObj.getString("phase_Name") + ",");
					if (pObj.has("md")) {
						csv.append(pObj.getString("md") + ",");
						csv.append(pObj.getString("tvd") + ",");
						csv.append(pObj.getString("inclination") + ",");
						csv.append(pObj.getString("activity_Category") + ",");
						if (pObj.getString("activity_Category").equals("Off Bottom")) {
							csv.append(pObj.getString("off_Bottom") + ",,");
						} else {
							csv.append("," + pObj.getString("on_Bottom") + ",");
						}
						csv.append(pObj.getString("date") + ",");
						csv.append(pObj.getString("time_From") + ",");
						csv.append(pObj.getString("time_To") + ",");
						csv.append(pObj.getString("hours_taken_for_operation") + "\n");
					} else {
						csv.append("\n");
					}
				}

			}
			return csv.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV2(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			csv.append("S.No,Phase Name,Level 2\n");

			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phase_Name")) {
					csv.append(
							pObj.getString("s_No") + "," + pObj.getString("phase_Name") + ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("level_1")) {
						JSONArray l1Ar = (JSONArray) pObj.get("level_1");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							csv.append(l1Obj.getString("s_No") + ",," + l1Obj.getString("level_2") + ",");
							if (l1Obj.has("md")) {
								csv.append(l1Obj.getString("md") + ",");
								csv.append(l1Obj.getString("tvd") + ",");
								csv.append(l1Obj.getString("inclination") + ",");
								csv.append(l1Obj.getString("activity_Category") + ",");
								if (l1Obj.getString("activity_Category").equals("Off Bottom")) {
									csv.append(l1Obj.getString("off_Bottom") + ",,");
								} else {
									csv.append("," + l1Obj.getString("on_Bottom") + ",");
								}
								csv.append(l1Obj.getString("date") + ",");
								csv.append(l1Obj.getString("time_From") + ",");
								csv.append(l1Obj.getString("time_To") + ",");
								csv.append(l1Obj.getString("hours_taken_for_operation") + "\n");
							} else {
								csv.append("\n");
							}
						}

					}
				}
			}
			return csv.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV12(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			// System.out.println("test length are::"+obj.get(0).toString().contains("MD"));

			csv.append("S.No,Phase Name,Level 2,Level 3\n");

			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phase_Name")) {
					csv.append(
							pObj.getString("s_No") + "," + pObj.getString("phase_Name") + ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("level_1")) {
						JSONArray l1Ar = (JSONArray) pObj.get("level_1");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("level_2")) {
								csv.append(l1Obj.getString("s_No") + ",," + l1Obj.getString("level_2")
										+ ",,,,,,,,,,,,,,,,,,,,,,\n");
							}
							if (l1Obj.has("level_3") && !l1Obj.get("level_3").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("level_3");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									csv.append(l3Obj.getString("s_No") + ",,," + l3Obj.getString("level_3") + ",");
									if (l3Obj.has("md")) {
										csv.append(l3Obj.getString("md") + ",");
										csv.append(l3Obj.getString("tvd") + ",");
										csv.append(l3Obj.getString("inclination") + ",");
										csv.append(l3Obj.getString("activity_Category") + ",");
										if (l3Obj.getString("activity_Category").equals("Off Bottom")) {
											csv.append(l3Obj.getString("off_Bottom") + ",,");
										} else {
											csv.append("," + l3Obj.getString("on_Bottom") + ",");
										}
										csv.append(l3Obj.getString("date") + ",");
										csv.append(l3Obj.getString("time_From") + ",");
										csv.append(l3Obj.getString("time_To") + ",");
										csv.append(l3Obj.getString("hours_taken_for_operation") + "\n");
									} else {
										csv.append("\n");
									}
								}
							}
						}
					}
				}
			}
			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV4(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);
			System.out.println("json are" + wellInputJson);
			csv.append("S.No,Phase Name,Level 2,Level 3,Level 4\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phase_Name")) {
					csv.append(
							pObj.getString("s_No") + "," + pObj.getString("phase_Name") + ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("level_1")) {
						JSONArray l1Ar = (JSONArray) pObj.get("level_1");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("level_2")) {
								csv.append(l1Obj.getString("s_No") + ",," + l1Obj.getString("level_2")
										+ ",,,,,,,,,,,,,,,,,,,,,,\n");
							}
							if (l1Obj.has("level_3") && !l1Obj.get("level_3").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("level_3");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									if (l3Obj.has("level_3")) {
										csv.append(l3Obj.getString("s_No") + ",,," + l3Obj.getString("level_3")
												+ ",,,,,,,,,,,,,,,,,,,,,,\n");
									}
									if (l3Obj.has("level_4")) {
										JSONArray l4Ar = (JSONArray) l3Obj.get("level_4");
										for (int l = 0; l < l4Ar.length(); l++) {
											JSONObject l4Obj = (JSONObject) l4Ar.get(l);
											if (l4Obj.has("level_4")) {
												csv.append(l4Obj.getString("s_No") + ",,,," + l4Obj.getString("level_4")
														+ ",");
												if (l4Obj.has("md")) {
													csv.append(l4Obj.getString("md") + ",");
													csv.append(l4Obj.getString("tvd") + ",");
													csv.append(l4Obj.getString("inclination") + ",");
													csv.append(l4Obj.getString("activity_Category") + ",");
													if (l4Obj.getString("activity_Category").equals("Off Bottom")) {
														csv.append(l4Obj.getString("off_Bottom") + ",,");
													} else {
														csv.append("," + l4Obj.getString("on_Bottom") + ",");
													}
													csv.append(l4Obj.getString("date") + ",");
													csv.append(l4Obj.getString("time_From") + ",");
													csv.append(l4Obj.getString("time_To") + ",");
													csv.append(l4Obj.getString("hours_taken_for_operation") + "\n");
												} else {
													csv.append("\n");
												}
											}

										}

									}
								}
							}
						}
					}
				}
			}
			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToCSV5(String wellInputJson) {
		try {
			StringBuffer csv = new StringBuffer();
			JSONArray obj = new JSONArray(wellInputJson);

			csv.append("S.No,Phase Name,Level 2,Level 3,Level 4,Level 5\n");
			for (int i = 0; i < obj.length(); i++) {
				JSONObject pObj = (JSONObject) obj.get(i);
				if (pObj.has("phase_Name")) {
					csv.append(
							pObj.getString("s_No") + "," + pObj.getString("phase_Name") + ",,,,,,,,,,,,,,,,,,,,,,,\n");
					if (pObj.has("level_1")) {
						JSONArray l1Ar = (JSONArray) pObj.get("level_1");
						for (int j = 0; j < l1Ar.length(); j++) {
							JSONObject l1Obj = (JSONObject) l1Ar.get(j);
							if (l1Obj.has("level_2")) {
								csv.append(l1Obj.getString("s_No") + ",," + l1Obj.getString("level_2")
										+ ",,,,,,,,,,,,,,,,,,,,,,\n");
							}
							if (l1Obj.has("level_3") && !l1Obj.get("level_3").equals("null")) {
								JSONArray l3Ar = (JSONArray) l1Obj.get("level_3");
								for (int k = 0; k < l3Ar.length(); k++) {
									JSONObject l3Obj = (JSONObject) l3Ar.get(k);
									if (l3Obj.has("level_3")) {
										csv.append(l3Obj.getString("s_No") + ",,," + l3Obj.getString("level_3")
												+ ",,,,,,,,,,,,,,,,,,,,,,\n");
									}
									if (l3Obj.has("level_4")) {
										JSONArray l4Ar = (JSONArray) l3Obj.get("level_4");
										for (int l = 0; l < l4Ar.length(); l++) {
											JSONObject l4Obj = (JSONObject) l4Ar.get(l);
											if (l4Obj.has("level_4")) {
												csv.append(l4Obj.getString("s_No") + ",,,," + l4Obj.getString("level_4")
														+ ",,,,,,,,,,,,,,,,,,,,,,\n");
											}
											if (l4Obj.has("level_5")) {
												JSONArray l5Ar = (JSONArray) l4Obj.get("level_5");
												for (int m = 0; m < l5Ar.length(); m++) {
													JSONObject l5Obj = (JSONObject) l5Ar.get(m);
													if (l5Obj.has("level_5")) {
														csv.append(l5Obj.getString("s_No") + ",,,,,"
																+ l5Obj.getString("level_5") + ",");
														if (l5Obj.has("md")) {
															csv.append(l5Obj.getString("md") + ",");
															csv.append(l5Obj.getString("tvd") + ",");
															csv.append(l5Obj.getString("inclination") + ",");
															csv.append(l5Obj.getString("activity_Category") + ",");
															if (l5Obj.getString("activity_Category")
																	.equals("Off Bottom")) {
																csv.append(l5Obj.getString("off_Bottom") + ",,");
															} else {
																csv.append("," + l5Obj.getString("on_Bottom") + ",");
															}
															csv.append(l5Obj.getString("date") + ",");
															csv.append(l5Obj.getString("time_From") + ",");
															csv.append(l5Obj.getString("time_To") + ",");
															csv.append(l5Obj.getString("hours_taken_for_operation")
																	+ "\n");
														} else {
															csv.append("\n");
														}

													}
												}

											}
										}

									}
								}
							}
						}
					}
				}
			}

			return csv.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String convertWellInputObjToJSON(String wellInputObjJson) {
		return toJSON(convertWellInputObjToCSV12(wellInputObjJson));
	}

	public static String convertWellInputCSVToObj(String wellInputCSV) throws IOException {
		List<String> lines = Arrays.asList(wellInputCSV.split("\\r?\\n"));

		// System.out.println("lines size are" + lines.size());

		return convertWellInputToObj13(lines);
	}

	public static String convertWellInputCSVToObj1(String wellInputCSV) throws IOException {
		List<String> lines = Arrays.asList(wellInputCSV.split("\\r?\\n"));
		return convertWellInputToObj1(lines);
	}

	public static String convertWellInputCSVToObj2(String wellInputCSV) throws IOException {
		List<String> lines = Arrays.asList(wellInputCSV.split("\\r?\\n"));
		System.out.println(lines);
		return convertWellInput2ToObj(lines);
	}

	public static String convertWellInputCSVToObj4(String wellInputCSV) throws IOException {
		List<String> lines = Arrays.asList(wellInputCSV.split("\\r?\\n"));
		return convertWellInput4ToObj(lines);
	}

	public static String convertWellInputCSVToObj5(String wellInputCSV) throws IOException {
		List<String> lines = Arrays.asList(wellInputCSV.split("\\r?\\n"));
		return convertWellInput5ToObj(lines);
	}

	public static String toCSV(JSONArray jAr) {
		StringBuffer result = new StringBuffer();

		try {
			for (int i = 0; i < jAr.length(); i++) {
				JSONArray jAr1 = (JSONArray) jAr.get(i);
				for (int j = 0; j < jAr1.length(); j++) {
					result.append(jAr1.getString(j) + ((j == jAr1.length() - 1) ? "" : ","));
				}
				result.append((i == jAr.length() - 1) ? "" : "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(result.toString());
		return result.toString();
	}

	public static String toJSON(String csv) {
		List<String> lines = Arrays.asList(csv.split("\\r?\\n"));
		Iterator<String> itr = lines.iterator();
		System.out.println("stage1" + lines.size());
		JSONArray jAr = new JSONArray();
		while (itr.hasNext()) {
			String line = itr.next();
			JSONArray jArIn = new JSONArray(Arrays.asList(line.split(",", -1)));
			jAr.put(jArIn);
		}
		System.out.println("jsonarray" + jAr);
		return jAr.toString();
	}

	public static String convertWellInputFileToObj(List<MultipartFile> uploadfiles) throws IOException {
		BufferedReader br;
		List<String> inCSV = new ArrayList<>();

		String inputObjJson = "";

		for (MultipartFile multipart : uploadfiles) {
			if (multipart.isEmpty()) {
				continue;
			}

			String line;
			InputStream is = multipart.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				inCSV.add(line);
			}

			inputObjJson = convertWellInputToObj(inCSV);
		}

		return inputObjJson;

	}

	public static String convertWellInputFileToObjtest(List<MultipartFile> uploadfiles, String levels)
			throws IOException {
		BufferedReader br;
		List<String> inCSV = new ArrayList<>();

		String inputObjJson = "";

		for (MultipartFile multipart : uploadfiles) {
			if (multipart.isEmpty()) {
				continue;
			}

			String line;
			InputStream is = multipart.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				inCSV.add(line);
			}
			if (levels.equals("1")) {
				inputObjJson = convertWellInputToObj1(inCSV);

			} else if (levels.equals("2")) {
				inputObjJson = convertWellInput2ToObj(inCSV);

			} else if (levels.equals("3")) {
				inputObjJson = convertWellInputToObj13(inCSV);

			} else if (levels.equals("4")) {
				inputObjJson = convertWellInput4ToObj(inCSV);

			} else {
				inputObjJson = convertWellInput5ToObj(inCSV);

			}
		}

		return inputObjJson;

	}

	public static String convertWellInputToObj1(List<String> inCSV1) throws IOException {
		List<WellInputLevel> wellInputPhases = new ArrayList<WellInputLevel>();
//		Set<WellParameters> wellParameters = new HashSet<WellParameters>();

		WellInputLevel wellInputPhase = null;
//		WellParameters wellParameter = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		for (int i = 0; i < inCSV1.size(); i++) {
			String cLine = inCSV1.get(i);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
				if (!lines.get(1).trim().equals("")) {

//			wellInputPhase.setLevel_1(new ArrayList<WellInputLevel2Node>(null));
					wellInputPhase = new WellInputLevel();
					//wellInputPhase.setLevelValue((Integer.parseInt((lines.get(0)))));
					wellInputPhase.setPhaseName((lines.get(1)));
					if (lines.size() > 2) {

						wellInputPhase.setMD(lines.get(2));
						wellInputPhase.setTVD(lines.get(3));
						wellInputPhase.setInclination(lines.get(4));
						wellInputPhase.setActivity_Category(lines.get(5));
						wellInputPhase.setOff_Bottom(lines.get(6));
						wellInputPhase.setOn_Bottom(lines.get(7));
						wellInputPhase.setDate(lines.get(8));
						wellInputPhase.setTime_From(lines.get(9));
						wellInputPhase.setTime_To(lines.get(10));
						wellInputPhase.setHours_taken_for_operation(lines.get(11));

					}
					wellInputPhases.add(wellInputPhase);
					wellInputPhase = null;
				}
			}

			if (i == inCSV1.size() - 1) {
				if (wellInputPhase != null) {
					wellInputPhases.add(wellInputPhase);

				}
			}

		}

		return mapper.writeValueAsString(wellInputPhases);

	}

	public static String convertWellInput2ToObj(List<String> inCSV) throws IOException {
		List<WellInputLevel> wellInputPhases = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel2Nodes = new ArrayList<WellInputLevel>();
		WellInputLevel wellInputPhase = null;

//		WellParameters wellParameter = null;
		WellInputLevel wellInputLevel2Node = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		for (int i = 0; i < inCSV.size(); i++) {
			String cLine = inCSV.get(i);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
				if (!lines.get(1).trim().equals("")) {
					if (wellInputPhase != null) {
						if (wellInputLevel2Nodes.size() != 0)
							wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
						wellInputPhases.add(wellInputPhase);
						wellInputLevel2Nodes.clear();

					}
					wellInputPhase = new WellInputLevel();
					//wellInputPhase.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputPhase.setPhaseName(lines.get(1));
				} else if (!lines.get(2).trim().equals("")) {
					if (wellInputLevel2Node != null) {
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						wellInputLevel2Nodes.clear();

					} else {
						wellInputLevel2Node = new WellInputLevel();
						//wellInputLevel2Node.setLevelValue(Integer.parseInt((lines.get(0))));
						wellInputLevel2Node.setPhaseName((lines.get(2)));
//					wellParameter=new WellParameters();
//					wellParameter.setMD(lines.get(4));
//					wellParameter.setTVD(lines.get(5));
//					wellParameter.setInclination(lines.get(6));
//					wellParameter.setActivity_Category(lines.get(7));
//					wellParameter.setOff_Bottom(lines.get(8));
//					wellParameter.setOn_Bottom(lines.get(9));
//					wellParameter.setDate(lines.get(10));
//					wellParameter.setTime_From(lines.get(11));
//					wellParameter.setTime_To(lines.get(12));
//					wellParameter.setHours_taken_for_operation(lines
//							.get(13));
//					wellParameters.add(wellParameter);
						if (lines.size() > 3) {

							wellInputLevel2Node.setMD(lines.get(3));
							wellInputLevel2Node.setTVD(lines.get(4));
							wellInputLevel2Node.setInclination(lines.get(5));
							wellInputLevel2Node.setActivity_Category(lines.get(6));
							wellInputLevel2Node.setOff_Bottom(lines.get(7));
							wellInputLevel2Node.setOn_Bottom(lines.get(18));
							wellInputLevel2Node.setDate(lines.get(9));
							wellInputLevel2Node.setTime_From(lines.get(10));
							wellInputLevel2Node.setTime_To(lines.get(11));
							wellInputLevel2Node.setHours_taken_for_operation(lines.get(12));

						}
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						wellInputLevel2Node = null;
					}
				}
			}
			if (i == inCSV.size() - 1) {

				if (wellInputPhase != null) {
					if (wellInputLevel2Nodes.size() != 0)
						wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
					wellInputPhases.add(wellInputPhase);
					wellInputLevel2Nodes.clear();
				}
			}
		}
//		System.out.println(wellInputPhases);
		return mapper.writeValueAsString(wellInputPhases);
	}

	public static String convertWellInput4ToObj(List<String> inCSV) throws IOException {

		List<WellInputLevel> wellInputPhases = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel2Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel3Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel4Nodes = new ArrayList<WellInputLevel>();
		// Set<WellParameters> wellParameters = new HashSet<WellParameters>();
//		Set<WellInputPhase> hs = new HashSet<>();

		WellParameters wellParameter = null;
		WellInputLevel wellInputPhase = null;
		WellInputLevel wellInputLevel2Node = null;
		WellInputLevel wellInputLevel3Node = null;
		WellInputLevel wellInputLevel4Node = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		for (int i = 0; i < inCSV.size(); i++) {
			String cLine = inCSV.get(i);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
				if (!lines.get(1).trim().equals("")) {
					if (wellInputPhase != null) {
						if (wellInputLevel3Node != null) {
							if (wellInputLevel4Nodes.size() != 0)
								wellInputLevel3Node.setSubLevels((new ArrayList<WellInputLevel>(wellInputLevel4Nodes)));
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							wellInputLevel3Node = null;
						}
						if (wellInputLevel2Node != null) {
							if (wellInputLevel3Nodes.size() != 0)
								wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
							wellInputLevel2Nodes.add(wellInputLevel2Node);
							wellInputLevel2Node = null;
						}
						if (wellInputLevel2Nodes.size() != 0)
							wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
						wellInputPhases.add(wellInputPhase);
						wellInputLevel2Nodes.clear();
						wellInputLevel3Nodes.clear();
						wellInputLevel4Nodes.clear();
					}
					wellInputPhase = new WellInputLevel();
					//wellInputPhase.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputPhase.setPhaseName((lines.get(1)));
				} else if (!lines.get(2).trim().equals("")) {
					if (wellInputLevel2Node != null) {
						if (wellInputLevel3Node != null) {
							if (wellInputLevel4Nodes.size() != 0)
								wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							wellInputLevel3Node = null;
							wellInputLevel4Nodes.clear();
						}
						if (wellInputLevel3Nodes.size() != 0)
							wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						wellInputLevel2Node = null;
						wellInputLevel3Nodes.clear();
					}
					wellInputLevel2Node = new WellInputLevel();
					//wellInputLevel2Node.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputLevel2Node.setPhaseName((lines.get(2)));
				} else if (!lines.get(3).trim().equals("")) {
					if (wellInputLevel3Node != null) {
						if (wellInputLevel4Nodes.size() != 0)
							wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
						wellInputLevel3Nodes.add(wellInputLevel3Node);
						wellInputLevel3Node = null;
						wellInputLevel4Nodes.clear();
					}

					wellInputLevel3Node = new WellInputLevel();
					//wellInputLevel3Node.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputLevel3Node.setPhaseName((lines.get(3)));

//					wellInputLevel3Nodes.add(wellInputLevel3Node);
				} else {
					if (wellInputLevel4Node != null) {
						wellInputLevel4Nodes.add(wellInputLevel4Node);
					} else {
						wellInputLevel4Node = new WellInputLevel();
						//wellInputLevel4Node.setLevelValue(Integer.parseInt((lines.get(0))));
						wellInputLevel4Node.setPhaseName((lines.get(4)));
					}

//					wellParameter=new WellParameters();
//					wellParameter.setMD(lines.get(4));
//					wellParameter.setTVD(lines.get(5));
//					wellParameter.setInclination(lines.get(6));
//					wellParameter.setActivity_Category(lines.get(7));
//					wellParameter.setOff_Bottom(lines.get(8));
//					wellParameter.setOn_Bottom(lines.get(9));
//					wellParameter.setDate(lines.get(10));
//					wellParameter.setTime_From(lines.get(11));
//					wellParameter.setTime_To(lines.get(12));
//					wellParameter.setHours_taken_for_operation(lines
//							.get(13));
//					wellParameters.add(wellParameter);
					if (lines.size() > 5) {

						wellInputLevel4Node.setMD(lines.get(5));
						wellInputLevel4Node.setTVD(lines.get(6));
						wellInputLevel4Node.setInclination(lines.get(7));
						wellInputLevel4Node.setActivity_Category(lines.get(8));
						wellInputLevel4Node.setOff_Bottom(lines.get(9));
						wellInputLevel4Node.setOn_Bottom(lines.get(10));
						wellInputLevel4Node.setDate(lines.get(11));
						wellInputLevel4Node.setTime_From(lines.get(12));
						wellInputLevel4Node.setTime_To(lines.get(13));
						wellInputLevel4Node.setHours_taken_for_operation(lines.get(14));

					}
					wellInputLevel4Nodes.add(wellInputLevel4Node);
					wellInputLevel4Node = null;

				}

			}
			if (i == inCSV.size() - 1) {

				if (wellInputLevel3Node != null) {
					if (wellInputLevel4Nodes.size() != 0)
						wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
					wellInputLevel3Nodes.add(wellInputLevel3Node);
					wellInputLevel4Nodes.clear();
				}
				if (wellInputLevel2Node != null) {
					if (wellInputLevel3Nodes.size() != 0)
						wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
					wellInputLevel2Nodes.add(wellInputLevel2Node);
					wellInputLevel3Nodes.clear();
				}
				if (wellInputPhase != null) {
					if (wellInputLevel2Nodes.size() != 0)
						wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
					wellInputPhases.add(wellInputPhase);
					wellInputLevel2Nodes.clear();
				}
			}

		}
		return mapper.writeValueAsString(wellInputPhases);
	}

	public static String convertWellInput5ToObj(List<String> inCSV) throws IOException {
		List<WellInputLevel> wellInputPhases = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel2Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel3Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel4Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel5Nodes = new ArrayList<WellInputLevel>();
//		Set<WellParameters> wellParameters = new HashSet<WellParameters>();
//		Set<WellInputPhase> hs = new HashSet<>();

		WellInputLevel wellInputPhase = null;
		WellInputLevel wellInputLevel2Node = null;
		WellInputLevel wellInputLevel3Node = null;
		WellInputLevel wellInputLevel4Node = null;
		WellInputLevel wellInputLevel5Node = null;
//		WellParameters wellParameter = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		for (int i = 0; i < inCSV.size(); i++) {
			String cLine = inCSV.get(i);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
				// System.out.println("line are:" + lines);
				if (!lines.get(0).trim().equals("")) {
					if (wellInputPhase != null) {
						if (wellInputLevel4Node != null) {
							if (wellInputLevel5Nodes.size() != 0)
								wellInputLevel4Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel5Nodes));
							wellInputLevel4Nodes.add(wellInputLevel4Node);
							wellInputLevel4Node = null;
							wellInputLevel5Nodes.clear();
						}
						if (wellInputLevel3Node != null) {
							if (wellInputLevel4Nodes.size() != 0)
								wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							wellInputLevel3Node = null;
							wellInputLevel4Nodes.clear();
						}
						if (wellInputLevel2Node != null) {
							if (wellInputLevel3Nodes.size() != 0)
								wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
							wellInputLevel2Nodes.add(wellInputLevel2Node);
							wellInputLevel2Node = null;
							wellInputLevel3Nodes.clear();
						}
						if (wellInputLevel2Nodes.size() != 0)
							wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
						wellInputPhases.add(wellInputPhase);
						wellInputLevel2Nodes.clear();
//						wellInputLevel3Nodes.clear();
//						wellInputLevel5Nodes.clear();

					}

					wellInputPhase = new WellInputLevel();
					//wellInputPhase.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputPhase.setPhaseName((lines.get(1)));
				} else if (!lines.get(2).trim().equals("")) {
					if (wellInputLevel2Node != null) {
						if (wellInputLevel4Node != null) {
							if (wellInputLevel5Nodes.size() != 0)
								wellInputLevel4Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel5Nodes));
							wellInputLevel4Nodes.add(wellInputLevel4Node);
							wellInputLevel4Node = null;
							wellInputLevel5Nodes.clear();

						}
						if (wellInputLevel3Node != null) {
							if (wellInputLevel4Nodes.size() != 0)
								wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							wellInputLevel3Node = null;
							wellInputLevel4Nodes.clear();

						}
//						if(wellInputLevel3Node != null) {
//							wellInputLevel3Nodes.add(wellInputLevel3Node);
//							wellInputLevel3Node = null;
//						}
						if (wellInputLevel3Nodes.size() != 0)
							wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						wellInputLevel2Node = null;
						wellInputLevel3Nodes.clear();
					}
					wellInputLevel2Node = new WellInputLevel();
					//wellInputLevel2Node.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputLevel2Node.setPhaseName((lines.get(2)));
				} else if (!lines.get(3).trim().equals("")) {
					if (wellInputLevel4Node != null) {
						if (wellInputLevel5Nodes.size() != 0)
							wellInputLevel4Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel5Nodes));
						wellInputLevel4Nodes.add(wellInputLevel4Node);
						wellInputLevel5Nodes.clear();
						wellInputLevel4Node = null;
					}
					if (wellInputLevel3Node != null) {
						if (wellInputLevel4Nodes.size() != 0)
							wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
						wellInputLevel3Nodes.add(wellInputLevel3Node);
						wellInputLevel3Node = null;
						wellInputLevel4Nodes.clear();
					}

					wellInputLevel3Node = new WellInputLevel();
					//wellInputLevel3Node.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputLevel3Node.setPhaseName((lines.get(3)));

					// wellInputLevel3Nodes.add(wellInputLevel3Node);
				} else if (!lines.get(4).trim().equals("")) {
					if (wellInputLevel4Node != null) {
						if (wellInputLevel5Nodes.size() != 0)
							wellInputLevel4Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel5Nodes));
						wellInputLevel4Nodes.add(wellInputLevel4Node);
						wellInputLevel4Node = null;
						wellInputLevel5Nodes.clear();
					}
					wellInputLevel4Node = new WellInputLevel();
					//wellInputLevel4Node.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputLevel4Node.setPhaseName((lines.get(4)));

				} else {
					if (wellInputLevel5Node != null) {
						wellInputLevel5Nodes.add(wellInputLevel5Node);
					} else {
						wellInputLevel5Node = new WellInputLevel();
						//wellInputLevel5Node.setLevelValue(Integer.parseInt((lines.get(0))));
						wellInputLevel5Node.setPhaseName((lines.get(5)));

//					wellParameter.setMD("");
//					wellParameter.setTVD("");
//					wellParameter.setInclination("");
//					wellParameter.setActivity_Category("");
//					wellParameter.setOff_Bottom("");
//					wellParameter.setOn_Bottom("");
//					wellParameter.setDate("");
//					wellParameter.setTime_From("");
//					wellParameter.setTime_To("");
//					wellParameter.setHours_taken_for_operation("");
//					wellParameters.add(wellParameter);
						if (lines.size() > 6) {

							wellInputLevel5Node.setMD(lines.get(6));
							wellInputLevel5Node.setTVD(lines.get(7));
							wellInputLevel5Node.setInclination(lines.get(8));
							wellInputLevel5Node.setActivity_Category(lines.get(9));
							wellInputLevel5Node.setOff_Bottom(lines.get(10));
							wellInputLevel5Node.setOn_Bottom(lines.get(11));
							wellInputLevel5Node.setDate(lines.get(12));
							wellInputLevel5Node.setTime_From(lines.get(13));
							wellInputLevel5Node.setTime_To(lines.get(14));
							wellInputLevel5Node.setHours_taken_for_operation(lines.get(15));

						}
						wellInputLevel5Nodes.add(wellInputLevel5Node);
						wellInputLevel5Node = null;
					}
					System.out.println("node are:" + wellInputLevel5Nodes.toString());

				}
			}

			if (i == inCSV.size() - 1) {
				if (wellInputLevel4Node != null) {
					if (wellInputLevel5Nodes.size() != 0)
						wellInputLevel4Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel5Nodes));
					wellInputLevel4Nodes.add(wellInputLevel4Node);
					wellInputLevel5Nodes.clear();
				}
				if (wellInputLevel3Node != null) {
					if (wellInputLevel4Nodes.size() != 0)
						wellInputLevel3Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel4Nodes));
					wellInputLevel3Nodes.add(wellInputLevel3Node);
					wellInputLevel4Nodes.clear();
				}
				if (wellInputLevel2Node != null) {
					if (wellInputLevel3Nodes.size() != 0)
						wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
					wellInputLevel2Nodes.add(wellInputLevel2Node);
					wellInputLevel3Nodes.clear();
				}
				if (wellInputPhase != null) {
					if (wellInputLevel2Nodes.size() != 0)
						wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
					wellInputPhases.add(wellInputPhase);
					wellInputLevel2Nodes.clear();
				}
			}
		}

		// System.out.println(mapper.writeValueAsString(wellInputPhases));
		return mapper.writeValueAsString(wellInputPhases);
	}

	public static String convertWellInputToObj(List<String> inCSV) throws IOException {
		List<WellInputPhase> wellInputPhases = new ArrayList<WellInputPhase>();
		List<WellInputLevel2Node> wellInputLevel2Nodes = new ArrayList<WellInputLevel2Node>();
		List<WellInputLevel3Node> wellInputLevel3Nodes = new ArrayList<WellInputLevel3Node>();
//		Set<WellParameters> wellParameters = new HashSet<WellParameters>();
		// Set<WellInputPhase> hs = new HashSet<>();

//		WellParameters wellParameter = null;
		WellInputPhase wellInputPhase = null;
		WellInputLevel2Node wellInputLevel2Node = null;
		WellInputLevel3Node wellInputLevel3Node = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		for (int i = 0; i < inCSV.size(); i++) {
			String cLine = inCSV.get(i);
			System.out.println("cLine" + cLine);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
				System.out.println("Lines :" + lines.size());
				if (!lines.get(1).trim().equals("")) {
					if (wellInputPhase != null) {
						if (wellInputLevel2Node != null) {
							if (wellInputLevel3Nodes.size() != 0)
								wellInputLevel2Node
										.setLevel_3(new ArrayList<WellInputLevel3Node>(wellInputLevel3Nodes));
							wellInputLevel2Nodes.add(wellInputLevel2Node);
							System.out.println("wellInputLevel2Nodes :" + wellInputLevel2Nodes);
							wellInputLevel2Node = null;
						}
						if (wellInputLevel2Nodes.size() != 0)
							wellInputPhase.setLevel_1(new ArrayList<WellInputLevel2Node>(wellInputLevel2Nodes));
						wellInputPhases.add(wellInputPhase);
						System.out.println("wellInputPhases :" + wellInputPhases);
						wellInputLevel2Nodes.clear();
						wellInputLevel3Nodes.clear();
					}
					wellInputPhase = new WellInputPhase();
					wellInputPhase.setS_No(lines.get(0));
					wellInputPhase.setPhase_Name(lines.get(1));
				} else if (!lines.get(2).trim().equals("")) {
					if (wellInputLevel2Node != null) {
						if (wellInputLevel3Node != null) {
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							System.out.println("wellInputLevel3Nodes :" + wellInputLevel3Nodes.toString());
							wellInputLevel3Node = null;
						}
						if (wellInputLevel3Nodes.size() != 0)
							wellInputLevel2Node.setLevel_3(new ArrayList<WellInputLevel3Node>(wellInputLevel3Nodes));
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						System.out.println("wellInputLevel2Nodes1 :" + wellInputLevel2Nodes);
						wellInputLevel2Node = null;
						wellInputLevel3Nodes.clear();
					}
					wellInputLevel2Node = new WellInputLevel2Node();
					wellInputLevel2Node.setS_No(lines.get(0));
					wellInputLevel2Node.setLevel_2(lines.get(2));
				} else {
					if (wellInputLevel3Node != null) {
						wellInputLevel3Node.setS_No(lines.get(0));
						wellInputLevel3Node.setLevel_3(lines.get(3));
					} else {
						wellInputLevel3Node = new WellInputLevel3Node();
						wellInputLevel3Node.setS_No(lines.get(0));
						wellInputLevel3Node.setLevel_3(lines.get(3));
					}
					if (lines.size() > 4) {

						wellInputLevel3Node.setMD(lines.get(4));
						wellInputLevel3Node.setTVD(lines.get(5));
						wellInputLevel3Node.setInclination(lines.get(6));
						wellInputLevel3Node.setActivity_Category(lines.get(7));
						wellInputLevel3Node.setOff_Bottom(lines.get(8));
						wellInputLevel3Node.setOn_Bottom(lines.get(9));
						wellInputLevel3Node.setDate(lines.get(10));
						wellInputLevel3Node.setTime_From(lines.get(11));
						wellInputLevel3Node.setTime_To(lines.get(12));
						wellInputLevel3Node.setHours_taken_for_operation(lines.get(13));

					}
					wellInputLevel3Nodes.add(wellInputLevel3Node);

//						wellParameters.add(wellParameter);

				}
				System.out.println("wellInputLevel3Nodes1 :" + wellInputLevel3Nodes.toString());

			}

			if (i == inCSV.size() - 1) {
				if (wellInputLevel2Node != null) {
					if (wellInputLevel3Nodes.size() != 0)
						wellInputLevel2Node.setLevel_3(new ArrayList<WellInputLevel3Node>(wellInputLevel3Nodes));
					wellInputLevel2Nodes.add(wellInputLevel2Node);
					wellInputLevel3Nodes.clear();
				}

				if (wellInputPhase != null) {
					if (wellInputLevel2Nodes.size() != 0)
						wellInputPhase.setLevel_1(new ArrayList<WellInputLevel2Node>(wellInputLevel2Nodes));
					wellInputPhases.add(wellInputPhase);
					wellInputLevel2Nodes.clear();
				}
			}
		}

//		System.out.println(mapper.writeValueAsString(wellInputPhases));
		return mapper.writeValueAsString(wellInputPhases);
	}

	public static String convertWellInputToObj13(List<String> inCSV) throws IOException {
		List<WellInputLevel> wellInputPhases = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel2Nodes = new ArrayList<WellInputLevel>();
		List<WellInputLevel> wellInputLevel3Nodes = new ArrayList<WellInputLevel>();

		WellInputLevel wellInputPhase = null;
		WellInputLevel wellInputLevel2Node = null;
		WellInputLevel wellInputLevel3Node = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		// System.out.println(inCSV.get(0));

		for (int i = 0; i < inCSV.size(); i++) {
			String cLine = inCSV.get(i);
			// System.out.println("cLine" + cLine);
			if (i != 0) {
				List<String> lines = Arrays.asList(cLine.split(",", -1));
//				System.out.println("Line" + lines);
//				System.out.println("Line size" + lines.size());
				if (!lines.get(1).trim().equals("")) {
					if (wellInputPhase != null) {
						if (wellInputLevel2Node != null) {
							if (wellInputLevel3Nodes.size() != 0)
								wellInputLevel2Node.setSubLevels((new ArrayList<WellInputLevel>(wellInputLevel3Nodes)));
							wellInputLevel2Nodes.add(wellInputLevel2Node);
							// System.out.println("first value" +
							// mapper.writeValueAsString(wellInputLevel2Nodes));
							wellInputLevel2Node = null;
						}
						if (wellInputLevel2Nodes.size() != 0)
							wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
						wellInputPhases.add(wellInputPhase);
						// System.out.println("second value" +
						// mapper.writeValueAsString(wellInputLevel2Nodes));
						wellInputLevel2Nodes.clear();
						wellInputLevel3Nodes.clear();
					}
					wellInputPhase = new WellInputLevel();
					//wellInputPhase.setLevelValue(Integer.parseInt((lines.get(0))));
					wellInputPhase.setPhaseName((lines.get(1)));
//					System.out.println("level2node" + mapper.writeValueAsString(wellInputLevel2Nodes));
//					System.out.println("level1node" + mapper.writeValueAsString(wellInputPhase));
				} else if (!lines.get(2).trim().equals("")) {
					if (wellInputLevel2Node != null) {
						if (wellInputLevel3Node != null) {
							wellInputLevel3Nodes.add(wellInputLevel3Node);
							wellInputLevel3Node = null;
						}
						if (wellInputLevel3Nodes.size() != 0)
							wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
						wellInputLevel2Nodes.add(wellInputLevel2Node);
						wellInputLevel2Node = null;
						wellInputLevel3Nodes.clear();
					}
					wellInputLevel2Node = new WellInputLevel();
					//wellInputLevel2Node.setLevelValue(Integer.parseInt(lines.get(0)));
					wellInputLevel2Node.setPhaseName((lines.get(2)));
				} else {
					if (wellInputLevel3Node != null) {
						wellInputLevel3Nodes.add(wellInputLevel3Node);
					} else {
						wellInputLevel3Node = new WellInputLevel();
						//wellInputLevel3Node.setLevelValue(Integer.parseInt((lines.get(0))));
						wellInputLevel3Node.setPhaseName((lines.get(3)));
						if (lines.size() > 4) {
							wellInputLevel3Node.setMD(lines.get(4));
							wellInputLevel3Node.setTVD(lines.get(5));
							wellInputLevel3Node.setInclination(lines.get(6));
							wellInputLevel3Node.setActivity_Category(lines.get(7));
							wellInputLevel3Node.setOff_Bottom(lines.get(8));
							wellInputLevel3Node.setOn_Bottom(lines.get(9));
							wellInputLevel3Node.setDate(lines.get(10));
							wellInputLevel3Node.setTime_From(lines.get(11));
							wellInputLevel3Node.setTime_To(lines.get(12));
							wellInputLevel3Node.setHours_taken_for_operation(lines.get(13));
						}
						wellInputLevel3Nodes.add(wellInputLevel3Node);
						wellInputLevel3Node = null;
						// System.out.println("level3node" +
						// mapper.writeValueAsString(wellInputLevel2Nodes));

					}
				}
			}

			if (i == inCSV.size() - 1) {
				if (wellInputLevel2Node != null) {
					if (wellInputLevel3Nodes.size() != 0)
						wellInputLevel2Node.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel3Nodes));
					wellInputLevel2Nodes.add(wellInputLevel2Node);
					System.out.println("level2node" + mapper.writeValueAsString(wellInputLevel2Nodes));
					wellInputLevel3Nodes.clear();
				}
				if (wellInputPhase != null) {
					if (wellInputLevel2Nodes.size() != 0)
						wellInputPhase.setSubLevels(new ArrayList<WellInputLevel>(wellInputLevel2Nodes));
					wellInputPhases.add(wellInputPhase);
					System.out.println("befor loop inner" + mapper.writeValueAsString(wellInputPhases));
					wellInputLevel2Nodes.clear();
				}
			}
		}
//		System.out.println("after loop outers" + (wellInputPhases));
//		System.out.println("after loop outer" + mapper.writeValueAsString(wellInputPhases));
		return mapper.writeValueAsString(wellInputPhases);
	}
}