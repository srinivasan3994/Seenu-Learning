package com.scs.controller;

import java.util.HashMap;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scs.entity.model.Ku;
import com.scs.exception.ApiException;
import com.scs.service.impl.ImportDbServicesImpl;
import com.scs.service.impl.KuDbServicesImpl;
import com.scs.util.ApiConstants;
import com.scs.util.ErrorConstants;
import com.scs.util.Utility;

@MultipartConfig(fileSizeThreshold = 20971520)

public class ImportController {

	private static final Logger logger = Logger.getLogger(KuController.class);
	private static final String CONTROLLER_END_EXCEPTION = "IMPORT CONTROLLER  ENDED WITH EXCEPTION";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ImportDbServicesImpl importDbService;

	/*@PostMapping(value = "/importNewKu", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object importKu(@RequestParam("file") MultipartFile multiparFile, HttpSession session) throws ApiException {

		HashMap<String, Object> responseObject = new HashMap<>();

		try {

			ObjectMapper mapper = new ObjectMapper();

			Ku ku = mapper.readValue(multiparFile.getInputStream(), Ku.class);
			importDbService.importNewKu(ku);

		} catch (ApiException ex) {
			logger.error(ex.getErrorCode() + " : " + ex.getErrorCode(), ex);
			logger.error(CONTROLLER_END_EXCEPTION);
			throw new ApiException(ex.getErrorCode(), ex.getErrorMessage());
		} catch (Exception ex) {
			logger.error(Utility.getExceptionMessage(ex));
			logger.error(CONTROLLER_END_EXCEPTION);
			throw new ApiException(ErrorConstants.SERVICEEXCEPTION, messageSource);
		}
		return responseObject.put("response", ApiConstants.SUCCESS);
	}*/
}
