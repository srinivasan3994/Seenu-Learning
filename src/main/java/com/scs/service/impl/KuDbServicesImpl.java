package com.scs.service.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scs.entity.model.Action;

import com.scs.entity.model.ActionExtn;

import com.scs.entity.model.Confirm;
import com.scs.entity.model.EntityDetails;
import com.scs.entity.model.EntityQuestion;
import com.scs.entity.model.EntityRegex;
import com.scs.entity.model.ErrorResponse;

import com.scs.entity.model.Intent;

import com.scs.entity.model.IntentExtn;
import com.scs.entity.model.Keyword;
import com.scs.entity.model.Ku;
import com.scs.entity.model.Languages;

import com.scs.entity.model.Message;
import com.scs.entity.model.ProjectKeyword;
import com.scs.entity.model.RegEx;
import com.scs.entity.model.RegexExtn;
import com.scs.entity.model.Response;
import com.scs.entity.model.UserInfo;
import com.scs.entity.model.WorkFlow;
import com.scs.exception.ApiException;
import com.scs.model.BCSettingsModel;
import com.scs.model.BaseRequestModel;

import com.scs.model.EntityModel;

import com.scs.model.IntentModel;
import com.scs.model.NodeDataArray;
import com.scs.model.RegexExtnModel;
import com.scs.model.SettingsModel;
import com.scs.model.WorkflowMetadataModel;
import com.scs.service.EntityDbServices;
import com.scs.service.IntentDbServices;
import com.scs.service.KuDbServices;
import com.scs.util.ApiConstants;
import com.scs.util.ErrorConstants;
import com.scs.util.Utility;

import jdk.nashorn.internal.ir.IfNode;

@Service("kuDbService")

public class KuDbServicesImpl implements KuDbServices {

	private static final Logger logger = Logger.getLogger(KuDbServicesImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private IntentDbServices intentDbService;

	@Autowired
	private EntityDbServices entityDbService;

	@Override
	public Object getKuDetails(BaseRequestModel baseModel) throws ApiException {

		Session session = null;
		List<Ku> kuLst = null;
		try {

			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Ku> query = builder.createQuery(Ku.class);
			Root<Ku> root = query.from(Ku.class);
			query.select(root);
			query.orderBy(builder.desc(root.get("id")));
			kuLst = session.createQuery(query).getResultList();

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);
		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return kuLst;
	}

	@Override
	public Object getKuById(BaseRequestModel baseModel, String id) throws ApiException {

		Session session = null;
		Ku ku = null;
		Set<RegEx> regexSet = new HashSet<>();
		List<EntityDetails> entity = new ArrayList<>();

		try {
			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Ku> select = builder.createQuery(Ku.class);
			Root<Ku> root = select.from(Ku.class);
			ParameterExpression<Long> kuId = builder.parameter(Long.class);
			Predicate kuIdP = builder.equal(root.get("id"), kuId);
			Predicate and1 = builder.and(kuIdP);
			select.where(and1);

			List<Ku> kuDetails = session.createQuery(select).setParameter(kuId, Long.parseLong(id)).getResultList();

			ku = kuDetails.get(0);

			regexSet = (Set<RegEx>) setRegexInKu(session, ku);
			entity = (List<EntityDetails>) setEntityRegexInKu(session, ku);

			ku.setEntities(entity);
			ku.setRegex(regexSet);

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);

		} finally {
			if (session != null) {
				session.close();
			}
		}

		return ku;
	}

	@Override
	public Object getKuByName(BaseRequestModel baseModel, String name) throws ApiException {

		Session session = null;
		Ku ku = null;

		try {
			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Ku> select = builder.createQuery(Ku.class);
			Root<Ku> root = select.from(Ku.class);
			ParameterExpression<String> kuId = builder.parameter(String.class);
			Predicate kuIdP = builder.equal(root.get("name"), kuId);
			Predicate and1 = builder.and(kuIdP);
			select.where(and1);

			List<Ku> kuDetails = session.createQuery(select).setParameter(kuId, name).getResultList();
			if (kuDetails.size() != 0) {
				ku = kuDetails.get(0);
			}

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);

		} finally {
			if (session != null) {
				session.close();
			}
		}

		return ku;
	}

	@Override
	public Object createKU(BaseRequestModel baseModel) throws ApiException {
		Session session = null;
		Transaction tx = null;
		Ku ku = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			ku = new Ku();
			ku.setId(null);
			ku.setName(Utility.CapsFirst(baseModel.getKu().getName()));
			ku.setActiveInd(baseModel.getKu().getActiveInd());
			ku.setSpamEnable("Y");
			ku.setIsRankable(baseModel.getKu().getIsRankable());

			session.save(ku);
			tx.commit();

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return ku;
	}

	@Override
	public Object updateKU(BaseRequestModel baseModel) throws ApiException {
		Session session = null;
		Transaction tx = null;
		String response = ApiConstants.SUCCESS;

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaUpdate<Ku> update = builder.createCriteriaUpdate(Ku.class);
			Root<Ku> root = update.from(Ku.class);
			update.set(root.get("name"), Utility.CapsFirst(baseModel.getKu().getName()));
			update.set(root.get("activeInd"), baseModel.getKu().getActiveInd());
			update.set(root.get("spamEnable"), "Y");
			update.set(root.get("isRankable"), baseModel.getKu().getIsRankable());
			update.where(builder.equal(root.get("id"), baseModel.getKu().getId()));
			session.createQuery(update).executeUpdate();
			tx.commit();

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.updateKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);
		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.updateKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return response;

	}

	@Override
	public Object deleteKU(BaseRequestModel baseModel, String id) throws ApiException {
		Session session = null;
		Transaction tx = null;

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Ku ku = new Ku();
			ku.setId(Long.parseLong(id));
			session.delete(ku);
			tx.commit();

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.deleteKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.deleteKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);

		} finally {
			if (session != null) {
				session.close();
			}
		}

		return null;
	}

	@Override
	public Object getUserByName(BaseRequestModel baseModel, String name) throws ApiException {

		Session session = null;
		UserInfo user = null;

		try {
			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<UserInfo> select = builder.createQuery(UserInfo.class);
			Root<UserInfo> root = select.from(UserInfo.class);
			ParameterExpression<String> userName = builder.parameter(String.class);
			Predicate kuIdP = builder.equal(root.get("username"), userName);
			Predicate and1 = builder.and(kuIdP);
			select.where(and1);

			List<UserInfo> userInfo = session.createQuery(select).setParameter(userName, name).getResultList();

			user = userInfo.get(0);

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKuById END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);

		} finally {
			if (session != null) {
				session.close();
			}
		}

		return user;
	}

	@Override
	public Object checkNames(BaseRequestModel baseModel, Ku ku) throws ApiException {
		Session session = null;
		Transaction tx = null;
		String response = ApiConstants.SUCCESS;

		try {
			Ku kus = null;

			kus = (Ku) getKuByName(baseModel, ku.getName());
			if (kus == null) {
				ku.setFlag(false);
			} else {
				ku.setFlag(true);
			}

			List<Intent> intentLsts = new ArrayList<>();

			List<Intent> intentLst = (List<Intent>) intentDbService.getintentDetails(baseModel);
			
			List<IntentExtn> intentNameLst = (List<IntentExtn>) intentDbService.getintentExtnDetails(baseModel);
			

			List<EntityDetails> entityLsts = new ArrayList<>();
			List<EntityDetails> entityLst = (List<EntityDetails>) entityDbService.getEntityDetails(baseModel);

			for (Intent intent : ku.getIntents()) {

				List<IntentExtn> names = new ArrayList<>();
				intent.setFlag(false);

				for (IntentExtn intentName : intent.getNames()) {

					intentName.setFlag(false);

					for (IntentExtn intentDtl : intentNameLst) {
						if (intentDtl.getName().toLowerCase().equals(intentName.getName().toLowerCase())) {

							intentName.setFlag(true);
							names.add(intentName);
							intent.setFlag(true);
							intent.setNames(names);

						}

					}

				}

				intentLsts.add(intent);

			}

			for (EntityDetails entity : ku.getEntities()) {

				entity.setFlag(false);

				for (EntityDetails entityDtl : entityLst) {
					if (entityDtl.getName().equals(entity.getName())) {

						entity.setFlag(true);

					}

				}

				entityLsts.add(entity);

			}

			ku.setIntents(intentLsts);
			ku.setEntities(entityLsts);

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			throw new ApiException(ErrorConstants.SERVICEEXCEPTION, messageSource);
		} catch (Exception ex) {
			response = Utility.checkUniqueConstraint(ex);
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITHEXCEPTION +++++");
			throw new ApiException(ErrorConstants.SERVICEEXCEPTION, messageSource);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return ku;

	}

	@Override
	public Object importKu(Ku ku) throws ApiException {
		Session session = null;
		Transaction tx = null;
		String response = ApiConstants.SUCCESS;
		Boolean nameidentifier = false;
		List<Intent> intentLst = new ArrayList<>();

		List<EntityModel> entityDetails = new ArrayList<EntityModel>();

		for (EntityDetails ed : ku.getEntities()) {
			if (ed.getIntentId() != null) {
				EntityModel entityModel = new EntityModel();
				entityModel.setId(ed.getId());
				entityModel.setName(ed.getName());

				String intentModelName = "";
				for (Intent intent : ku.getIntents()) {
					if (intent.getId() != null) {
						for (IntentExtn intentExtn : intent.getNames()) {
							if (intentExtn.getLocaleCode().equals("en") && ed.getIntentId() != null
									&& intent.getId() != null) {
								if (ed.getIntentId().equals(intent.getId())) {
									intentModelName = intentExtn.getName();
									System.out.println(intentModelName);
									entityModel.setIntentName(intentModelName);
								}
							}
						}
					}
				}
				String intentIdStr = ed.getIntentId() + "0";
				Long intentId = Long.parseLong(intentIdStr);
				entityModel.setIntentId(intentId);
				entityDetails.add(entityModel);
			}
		}
		List<IntentModel> intentModellst = new ArrayList<IntentModel>();
		for (Intent intent : ku.getIntents()) {
			IntentModel intentModel = new IntentModel();
			intentModel.setId(intent.getId());
			String intentModelName = "";
			for (IntentExtn intentExtn : intent.getNames()) {
				if (intentExtn.getLocaleCode().equals("en")) {
					intentModelName = intentExtn.getName();
					intentModel.setName(intentModelName);
				}
			}
			intentModellst.add(intentModel);
		}

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			List<Ku> kuLst = null;

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Ku> query = builder.createQuery(Ku.class);
			Root<Ku> root = query.from(Ku.class);
			query.select(root);
			query.orderBy(builder.asc(root.get("id")));

			kuLst = session.createQuery(query).getResultList();

			for (Ku kunam : kuLst) {

				if (kunam.getName().equals(ku.getName())) {
					nameidentifier = false;
					break;
				} else {
					nameidentifier = true;
				}
			}

			if (nameidentifier == true) {
				ku.setName(Utility.getCaseChangedName(ku.getName()));
				ku.setActiveInd(ku.getActiveInd());
				ku.setSpamEnable(ku.getSpamEnable());
				ku.setIsRankable(ku.getIsRankable());
				session.save(ku);

				intentLst = (List<Intent>) processFlowchart(session, ku);

				importEntities(session, ku);

				logger.error(
						"+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++" + entityDetails);

				importIntents(session, ku, intentLst, entityDetails);

				updateEntities(session, ku, entityDetails, intentModellst);

				importRegex(session, ku);

				tx.commit();

			} else {
				throw new ApiException("KU_EXIST", "KU already Exists.");
			}

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			throw new ApiException(ErrorConstants.SERVICEEXCEPTION, messageSource);
		} catch (ApiException ex) {
			logger.error(Utility.getExceptionMessage(ex));
			throw new ApiException(ex.getErrorCode(), ex.getErrorMessage());
		} catch (Exception ex) {
			response = Utility.checkUniqueConstraint(ex);
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITHEXCEPTION +++++");
			throw new ApiException(ErrorConstants.SERVICEEXCEPTION, messageSource);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return response;
	}

	@Override
	public Object importSettings(BCSettingsModel bcSettingsModel) throws ApiException {
		Session session = null;
		Transaction tx = null;
		String response = ApiConstants.SUCCESS;

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			Languages language = new Languages();

			Languages languageLst = (Languages) getLanguage();

			if (languageLst == null) {

				language.setId(bcSettingsModel.getSettings().getLanguage().getId());
				language.setEnglish(bcSettingsModel.getSettings().getLanguage().getEnglish());
				language.setArabic(bcSettingsModel.getSettings().getLanguage().getArabic());
				session.save(language);
			}

			List<ProjectKeyword> projectKeywordLst = (List<ProjectKeyword>) getProjectKeywordDetails();

			for (ProjectKeyword projectKey : bcSettingsModel.getSettings().getProjectKeywords()) {

				for (ProjectKeyword projectKeywordList : projectKeywordLst) {

					if (projectKeywordList.getProjectKeyword().contains(projectKey.getProjectKeyword())) {

						updateProjectKeywords(projectKey);

					} else {
						insertProjectKeywords(projectKey);

					}
				}

			}

			tx.commit();

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return response;
	}

	private void insertProjectKeywords(ProjectKeyword projectKey) throws ApiException {
		Session session = null;
		Transaction tx = null;

		List<ProjectKeyword> projectKeywords = new ArrayList<>();

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();

			ProjectKeyword projectKeyword = new ProjectKeyword();
			projectKeyword.setKeywordType(projectKey.getKeywordType());
			projectKeyword.setProjectKeyword(projectKey.getProjectKeyword());
			projectKeyword.setProjectId(null);
			projectKeyword.setLocaleCode(projectKey.getLocaleCode());
			session.save(projectKeyword);
			projectKeywords.add(projectKey);

			tx.commit();

		} catch (Exception ex) {

			logger.error(Utility.getExceptionMessage(ex));

		} finally {
			if (session != null) {
				session.close();
			}
		}

	}

	private void updateProjectKeywords(ProjectKeyword projectKey) throws ApiException {
		Session session = null;
		Transaction tx = null;
		String response = ApiConstants.SUCCESS;

		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaUpdate<ProjectKeyword> update = builder.createCriteriaUpdate(ProjectKeyword.class);
			Root<ProjectKeyword> root = update.from(ProjectKeyword.class);
			update.set(root.get("projectKeyword"), projectKey.getProjectKeyword());
			update.set(root.get("keywordType"), projectKey.getKeywordType());
			update.set(root.get("localeCode"), projectKey.getLocaleCode());
			update.where(builder.equal(root.get("id"), projectKey.getId()));

			session.createQuery(update).executeUpdate();
			tx.commit();

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.updateKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);
		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.updateKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

	}

	@Override
	public Object getSettings(BaseRequestModel baseModel) throws ApiException {
		Session session = null;

		BCSettingsModel settings = new BCSettingsModel();
		SettingsModel setModel = new SettingsModel();
		try {
			session = sessionFactory.openSession();

			Languages language = null;
			language = (Languages) getLanguage();

			List<ProjectKeyword> projectKeywordLst = null;
			projectKeywordLst = (List<ProjectKeyword>) getProjectKeywordDetails();

			setModel.setLanguage(language);
			setModel.setProjectKeywords(projectKeywordLst);

			settings.setSettings(setModel);

		} catch (HibernateException ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);

		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.createKU END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return settings;
	}

	@Override
	public Object getLanguage() throws ApiException {

		Session session = null;
		Languages language = null;
		try {

			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Languages> query = builder.createQuery(Languages.class);
			Root<Languages> root = query.from(Languages.class);
			query.select(root);
			query.orderBy(builder.desc(root.get("id")));
			language = session.createQuery(query).getResultList().get(0);

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);
		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return language;
	}

	@Override
	public Object getProjectKeywordDetails() throws ApiException {

		Session session = null;
		List<ProjectKeyword> projectKeywordLst = null;
		try {

			session = sessionFactory.openSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<ProjectKeyword> query = builder.createQuery(ProjectKeyword.class);
			Root<ProjectKeyword> root = query.from(ProjectKeyword.class);
			query.select(root);
			query.orderBy(builder.desc(root.get("id")));
			projectKeywordLst = session.createQuery(query).getResultList();

		} catch (HibernateException ex) {

			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITH Hibernate EXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonHibernateExceptionMethod(ex);
		} catch (Exception ex) {
			logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITHEXCEPTION +++++");
			logger.error(Utility.getExceptionMessage(ex));
			Utility.commonExceptionMethod(ex);
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return projectKeywordLst;
	}

	private Object processFlowchart(Session session, Ku ku)
			throws JsonParseException, JsonMappingException, IOException {

		WorkFlow workflow = new WorkFlow();
		List<NodeDataArray> nodeDataArrayList = new ArrayList<>();
		List<Intent> intentLst = new ArrayList<>();
		for (Intent intent : ku.getIntents()) {
			nodeDataArrayList = new ArrayList<>();
			workflow = intent.getWorkFlow();
			String metadata = workflow.getMetaData();

			ObjectMapper mapper = new ObjectMapper();

			WorkflowMetadataModel WorkflowMetadata = mapper.readValue(metadata, WorkflowMetadataModel.class);

			for (NodeDataArray nodeData : WorkflowMetadata.getNodeDataArray()) {

				if (nodeData.getModelid() == 5) {
					nodeData.getIntent().setName(intent.getName());
					nodeData.setText(intent.getName());

				}

				if (nodeData.getModelid() == 2) {

					for (EntityDetails entity : ku.getEntities()) {

						if (entity.getId().equals(nodeData.getEntity().getId())) {
							nodeData.getEntity().setName(entity.getName());

							nodeData.setText(entity.getName());

						}
					}

				}

				nodeDataArrayList.add(nodeData);

				WorkflowMetadata.setNodeDataArray(nodeDataArrayList);
				WorkflowMetadata.setClassvar(WorkflowMetadata.getClassvar());
				WorkflowMetadata.setLinkDataArray(WorkflowMetadata.getLinkDataArray());
				WorkflowMetadata.setLinkFromPortIdProperty(WorkflowMetadata.getLinkFromPortIdProperty());
				WorkflowMetadata.setLinkToPortIdProperty(WorkflowMetadata.getLinkToPortIdProperty());

			}

			String jsonInString = mapper.writeValueAsString(WorkflowMetadata);

			intent.getWorkFlow().setMetaData(jsonInString);

			intentLst.add(intent);

		}

		return intentLst;

	}

	private void importRegex(Session session, Ku ku) {

		for (RegEx regex : ku.getRegex()) {
			regex.setRegexname(regex.getRegexname());
			regex.setExpression(regex.getExpression());
			regex.setErrorCode(regex.getErrorCode());
			session.save(regex);

			for (RegexExtn regexextn : regex.getRegexes()) {

				RegexExtn regexExtn = new RegexExtn();

				regexExtn.setRegex(regex);
				regexExtn.setId(regexextn.getId());
				regexExtn.setErrorMessage(regexextn.getErrorMessage());
				regexExtn.setLocaleCode(regexextn.getLocaleCode());
				session.save(regexExtn);

			}

		}
	}

	/*
	 * private void importMapping(List<IntentEntity> intentMapping,
	 * List<IntentEntity> entityMapping, Ku ku, List<MapRegEx> regexMapping, Session
	 * session) {
	 * 
	 * for (IntentEntity intentMappingObj : intentMapping) { for (IntentEntity
	 * entityMappingObj : entityMapping) { if
	 * (intentMappingObj.getId().equals(entityMappingObj.getId())) { IntentEntity
	 * mapping = new IntentEntity();
	 * mapping.setEntity(entityMappingObj.getEntity());
	 * mapping.setIntent(intentMappingObj.getIntent()); mapping.setKuId(ku.getId());
	 * mapping.setRequired(entityMappingObj.getRequired());
	 * mapping.setOrderId(entityMappingObj.getOrderId()); session.save(mapping);
	 * 
	 * 
	 * } } }
	 * 
	 * }
	 */

	private void importEntities(Session session, Ku ku) {
		Instant instant = Instant.now();
		for (EntityDetails entity : ku.getEntities()) {
			entity.setEntityType(entity.getEntityType());
			entity.setKuId(ku.getId());
			entity.setGlobalIdentifier(UUID.randomUUID().toString());

			entity.setName(entity.getName());
			if (entity.getIntentId() != null) {
				String intentIdStr = entity.getIntentId() + "0";
				Long intentId = Long.parseLong(intentIdStr);
				entity.setIntentId(intentId);
			}

			entity.setDate(instant.toString());
			entity.setFlag(null);
			entity.setRequired(entity.getRequired());

			session.save(entity);

			importQuestions(session, entity);
			importEntityAction(session, entity);

		}
	}

	private void importQuestions(Session session, EntityDetails entity) {
		if (entity.getQuestions() != null) {
			for (EntityQuestion entityQuestion : entity.getQuestions()) {
				entityQuestion.setEntity(entity);
				entityQuestion.setQuestion(entityQuestion.getQuestion());
				entityQuestion.setExample(entityQuestion.getExample());
				entityQuestion.setLocaleCode(entityQuestion.getLocaleCode());
				entityQuestion.setTitle(entityQuestion.getTitle());
				entityQuestion.setSubTitle(entityQuestion.getSubTitle());
				entityQuestion.setButtonText(entityQuestion.getButtonText());
				entityQuestion.setImageUrl(entityQuestion.getImageUrl());

				session.save(entityQuestion);
			}
		}
	}

	private void importIntents(Session session, Ku ku, List<Intent> intentLst, List<EntityModel> entityDetails) {
		Instant instant = Instant.now();

		String intentName = "";
		for (Intent intent : intentLst) {

			for (IntentExtn intentExtn : intent.getNames()) {
				if (intentExtn.getLocaleCode().equals("en")) {
					intentName = intentExtn.getName();
				}
			}

			intent.setName(intentName);
			intent.setKuId(ku.getId());
			intent.setAction(intent.getAction());
			intent.setDate(instant.toString());
			intent.setFlag(null);
			intent.setGlobalIdentifier(UUID.randomUUID().toString());
			session.save(intent);

			importIntentExtn(session, intent);
			importKeywords(session, intent, intentName);
			importResponses(session, intent);
			importAction(session, intent, ku, entityDetails);
			importFlowchart(session, intent, intentName);

		}
	}

	private void importKeywords(Session session, Intent intent, String intentName) {
		Instant instant = Instant.now();
		Boolean intName = false;

		if (intent.getKeywords() != null) {
			for (Keyword keyword : intent.getKeywords()) {

				if ((keyword.getKeywordField()).equalsIgnoreCase(intentName)) {
					intName = true;

				}

				if (keyword.getKeywordField() != null) {
					keyword.setIntent(intent);
					keyword.setKeywordField(keyword.getKeywordField().toLowerCase());
					keyword.setPolarity(keyword.getPolarity());
					keyword.setLocaleCode(keyword.getLocaleCode());
					keyword.setDate(instant.toString());
					session.save(keyword);
				}
			}

			if (!intName) {

				for (IntentExtn intentExtn : intent.getNames()) {

					if (intentExtn.getName() != null) {
						Keyword keyword = new Keyword();
						keyword.setIntent(intent);
						keyword.setKeywordField(intentExtn.getName().toLowerCase());
						keyword.setPolarity("P");
						keyword.setLocaleCode(intentExtn.getLocaleCode());
						keyword.setDate(instant.toString());
						session.save(keyword);
					}
				}
			}
		}
	}

	private void importIntentExtn(Session session, Intent intent) {

		if (intent.getNames() != null) {
			for (IntentExtn intentExtn : intent.getNames()) {
				intentExtn.setIntent(intent);
				intentExtn.setLocaleCode(intentExtn.getLocaleCode());
				intentExtn.setName(intentExtn.getName());
				session.save(intentExtn);
			}
		}

	}

	private void importResponses(Session session, Intent intent) {

		Message message = new Message();
		message.setMessageCode(UUID.randomUUID().toString());

		if (intent.getResponses() != null) {
			for (Response response : intent.getResponses()) {
				response.setIntent(intent);
				response.setResponseText(response.getResponseText());
				response.setGlobalIdentifier(UUID.randomUUID().toString());
				response.setKuId(intent.getKuId());
				response.setMessage(message);
				session.save(response);
			}
		}
	}

	private void importAction(Session session, Intent intent, Ku ku, List<EntityModel> entityDetails) {
		List<Action> actionLst = intent.getAction();
		List<ActionExtn> actnExtnLst = new ArrayList<>();
		for (Action action : actionLst) {
			if (action.getName() != null) {

				action.setIntent(intent);

				action.setGlobalIdentifier(UUID.randomUUID().toString());
				action.setConfirm(action.getConfirm());
				session.save(action);

				for (ActionExtn actionExtnLst : action.getActionExtn()) {

					ActionExtn actionExtn = new ActionExtn();

					String text = actionExtnLst.getRequestBody();
					List<Long> entityLst = new ArrayList<Long>();
					Map<Long, String> entityfnlLst = new HashMap<Long, String>();
					List<String> entityNames = new ArrayList<String>();

					if (text != null) {

						String val1 = "";

						val1 = Utility.patternMatch(text);

						if (!val1.equals("")) {
							entityLst.add(Long.parseLong(val1));

							if (!entityLst.isEmpty()) {

								int eSize = entityLst.size() - 1;

								for (EntityModel entityDetail : entityDetails) {
									for (Long entityIds : entityLst) {
										if (entityIds.equals(entityDetail.getId())) {
											entityNames.add(entityDetail.getName());
										}
									}
								}

								if (!entityNames.isEmpty()) {
									int count = 0;
									for (EntityDetails entity : ku.getEntities()) {
										for (String entName : entityNames) {
											if (entity.getName().equals(entName)) {
												for (int i = 0; i < entityLst.size(); i++) {
													String ename = "[" + entity.getId() + "]";
													if (count == i) {
														entityfnlLst.put(entityLst.get(i), ename);
													}
												}
												count++;
											}
										}
									}
									String s = actionExtnLst.getRequestBody();
									List<String> entityLst1 = new ArrayList<String>();
									String val2 = "";

									val2 = Utility.patternMatch(s);
									entityLst1.add(val2);
									actionExtn.setRequestBody(entityLst1.get(eSize).toString());
								} else {
									actionExtn.setRequestBody(actionExtnLst.getRequestBody());
								}
							} else {
								actionExtn.setRequestBody(actionExtnLst.getRequestBody());
							}
						} else {
							actionExtn.setRequestBody(actionExtnLst.getRequestBody());
						}
					} else {
						actionExtn.setRequestBody(actionExtnLst.getRequestBody());
					}

					actionExtn.setCallMethod(actionExtnLst.getCallMethod());
					actionExtn.setErrorCode(actionExtnLst.getErrorCode());
					actionExtn.setLocaleCode(actionExtnLst.getLocaleCode());
					actionExtn.setResponsePath(actionExtnLst.getResponsePath());
					actionExtn.setSuccessCode(actionExtnLst.getSuccessCode());
					actionExtn.setAction(action);
					actionExtn.setUrl(actionExtnLst.getUrl());
					session.save(actionExtn);
					actnExtnLst.add(actionExtn);

				}
				action.setActionExtn(actnExtnLst);
				action.setName(action.getName());
				action.setKuId(intent.getKuId());
				session.save(action);
				importConfirmAction(session, action, ku, entityDetails);
				importErrorResponses(session, action);
			}
		}
	}

	private void importEntityAction(Session session, EntityDetails entityDetails) {
		Action action = entityDetails.getAction();
		List<ActionExtn> actnExtnLst = new ArrayList<>();
		if (action.getId() != null) {

			action.setEntity(entityDetails);

			action.setGlobalIdentifier(UUID.randomUUID().toString());
			action.setConfirm(action.getConfirm());
			action.setName(action.getName());
			action.setKuId(entityDetails.getKuId());
			session.save(action);

			for (ActionExtn actionExtnLst : action.getActionExtn()) {

				ActionExtn actionExtn = new ActionExtn();

				actionExtn.setRequestBody(actionExtnLst.getRequestBody());
				actionExtn.setCallMethod(actionExtnLst.getCallMethod());
				actionExtn.setErrorCode(actionExtnLst.getErrorCode());
				actionExtn.setLocaleCode(actionExtnLst.getLocaleCode());
				actionExtn.setResponsePath(actionExtnLst.getResponsePath());
				actionExtn.setSuccessCode(actionExtnLst.getSuccessCode());
				actionExtn.setAction(action);
				actionExtn.setUrl(actionExtnLst.getUrl());
				actnExtnLst.add(actionExtn);

				session.save(actionExtn);

			}

			importErrorResponses(session, action);

		}
	}

	private void importFlowchart(Session session, Intent intent, String intentName) {
		WorkFlow workflow = intent.getWorkFlow();
		String metadata = "";
		if (workflow != null) {

			try {
				workflow.setIntent(intent);
				workflow.setName(workflow.getName());
				metadata = (String) importMetadata(session, intent, intentName);
				workflow.setMetaData(metadata);
				workflow.setKuId(intent.getKuId());
				session.save(workflow);

			} catch (HibernateException ex) {

				logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITH Hibernate EXCEPTION +++++");
				logger.error(Utility.getExceptionMessage(ex));

			} catch (Exception ex) {
				logger.error("+++++ KuDbServicesImpl.getKUDetails END SERVICE WITHEXCEPTION +++++");
				logger.error(Utility.getExceptionMessage(ex));

			}

		}

	}

	private Object importMetadata(Session session, Intent intent, String intentName)
			throws JsonParseException, JsonMappingException, IOException {

		WorkFlow workflow = new WorkFlow();
		String jsonInString = null;
		List<NodeDataArray> nodeDataArrayList = new ArrayList<>();

		nodeDataArrayList = new ArrayList<>();
		workflow = intent.getWorkFlow();
		String metadata = workflow.getMetaData();

		ObjectMapper mapper = new ObjectMapper();

		WorkflowMetadataModel WorkflowMetadata = mapper.readValue(metadata, WorkflowMetadataModel.class);

		for (NodeDataArray nodeData : WorkflowMetadata.getNodeDataArray()) {

			if (nodeData.getModelid() == 5) {
				nodeData.getIntent().setName(intentName);
				nodeData.setText(intentName);

			}

			nodeDataArrayList.add(nodeData);

			WorkflowMetadata.setNodeDataArray(nodeDataArrayList);
			WorkflowMetadata.setClassvar(WorkflowMetadata.getClassvar());
			WorkflowMetadata.setLinkDataArray(WorkflowMetadata.getLinkDataArray());
			WorkflowMetadata.setLinkFromPortIdProperty(WorkflowMetadata.getLinkFromPortIdProperty());
			WorkflowMetadata.setLinkToPortIdProperty(WorkflowMetadata.getLinkToPortIdProperty());

		}

		jsonInString = mapper.writeValueAsString(WorkflowMetadata);

		return jsonInString;

	}

	private void importConfirmAction(Session session, Action action, Ku ku, List<EntityModel> entityDetails) {

		if (action.getConfirm() != null) {
			List<Confirm> confirmLst = action.getConfirm();

			for (Confirm confirm : confirmLst) {

				confirm.setAction(action);
				confirm.setKuId(action.getKuId());
				confirm.setConfirmationOption(confirm.getConfirmationOption());
				confirm.setConfirmationType(confirm.getConfirmationType());
				confirm.setLocaleCode(confirm.getLocaleCode());
				confirm.setTerminationText(confirm.getTerminationText());
				confirm.setUnConfirmationOption(confirm.getUnConfirmationOption());

				String text = confirm.getText();
				List<Long> entityLst = new ArrayList<Long>();
				Map<Long, String> entityfnlLst = new HashMap<Long, String>();
				List<String> entityNames = new ArrayList<String>();

				if (text != null) {

					Pattern pattern = Pattern.compile("(\\[\\d+\\])");
					Matcher matcher = pattern.matcher(text);

					while (matcher.find()) {
						String val1 = matcher.group().replace("[", "").replace("]", "");
						entityLst.add(Long.parseLong(val1));
					}

					if (!entityLst.isEmpty()) {

						int eSize = entityLst.size() - 1;

						for (EntityModel entityDetail : entityDetails) {
							for (Long entityIds : entityLst) {
								if (entityIds.equals(entityDetail.getId())) {
									entityNames.add(entityDetail.getName());
								}
							}
						}

						if (!entityNames.isEmpty()) {
							int count = 0;
							for (EntityDetails entity : ku.getEntities()) {
								for (String entName : entityNames) {
									if (entity.getName().equals(entName)) {
										for (int i = 0; i < entityLst.size(); i++) {
											String ename = "[" + entity.getId() + "]";
											if (count == i) {
												entityfnlLst.put(entityLst.get(i), ename);
											}

										}
										count++;
									}
								}
							}
							String s = confirm.getText();
							List<String> entityLst1 = new ArrayList<String>();
							Pattern p = Pattern.compile("(\\[\\d+\\])");
							Matcher m = p.matcher(s);
							while (m.find()) {
								String val1 = m.group().replace("[", "").replace("]", "");
								s = (s.replace(m.group(), entityfnlLst.get(Long.parseLong(val1))));
								entityLst1.add(s);
							}
							confirm.setText(entityLst1.get(eSize).toString());
						} else {
							confirm.setText(confirm.getText());
						}
					} else {
						confirm.setText(confirm.getText());
					}
				} else {
					confirm.setText(confirm.getText());
				}

				session.save(confirm);
			}
		}

	}

	private void updateEntities(Session session, Ku ku, List<EntityModel> entityDetails,
			List<IntentModel> intentModellst) {

		for (Intent intent : ku.getIntents()) {

			for (IntentModel intentModel : intentModellst) {

				if (intent.getName().equals(intentModel.getName())) {

					for (EntityModel entityModel : entityDetails) {

						if (intent.getName().equals(entityModel.getIntentName())) {

							for (EntityDetails entity : ku.getEntities()) {

								if (entity.getIntentId().equals(entityModel.getIntentId())) {
									entity.setIntentId(intent.getId());

									session.update(entity);
								}
							}

						}

					}

				}
			}
		}

	}

	private void importErrorResponses(Session session, Action action) {
		if (action.getErrorResponses() != null) {
			for (ErrorResponse errorResponse : action.getErrorResponses()) {
				errorResponse.setAction(action);
				errorResponse.setErrorCode(errorResponse.getErrorCode());
				errorResponse.setErrorResponse(errorResponse.getErrorResponse());
				errorResponse.setLocaleCode(errorResponse.getLocaleCode());
				errorResponse.setKuId(action.getKuId());
				session.save(errorResponse);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Object setRegexInKu(Session session, Ku ku) {

		List<RegEx> regexLst = new ArrayList<>();

		List<EntityDetails> entityLst = ku.getEntities();

		for (EntityDetails entity : entityLst) {

			List<EntityRegex> entityRegexLst = (List<EntityRegex>) getEntityRegex(session, ku, entity.getId());

			for (EntityRegex entityRegex : entityRegexLst) {

				RegEx regex = (RegEx) getRegex(session, ku, entityRegex.getRegexId());
				regexLst.add(regex);

			}

		}
		// regexSet = (Set<RegEx>) (RegEx);
		Set<RegEx> regexSet = new HashSet<RegEx>(regexLst);

		return regexSet;

	}

	@SuppressWarnings("unchecked")
	private Object setEntityRegexInKu(Session session, Ku ku) {

		List<EntityDetails> entityLstRespone = new ArrayList<>();

		List<EntityRegex> entityRegexLst = null;

		List<EntityDetails> entityLst = ku.getEntities();

		for (EntityDetails entity : entityLst) {

			entityRegexLst = (List<EntityRegex>) getEntityRegex(session, ku, entity.getId());

			EntityDetails entityDetails = new EntityDetails();

			entityDetails.setName(entity.getName());
			entityDetails.setEntityRegex(entityRegexLst);
			entityDetails.setAction(entity.getAction());
			entityDetails.setEntityType(entity.getEntityType());
			entityDetails.setExample(entity.getExample());
			entityDetails.setQuestions(entity.getQuestions());
			entityDetails.setGlobalIdentifier(entity.getGlobalIdentifier());
			entityDetails.setIntentId(entity.getIntentId());
			entityDetails.setKuId(entity.getKuId());
			entityDetails.setId(entity.getId());
			entityDetails.setDate(entity.getDate());

			entityLstRespone.add(entityDetails);

		}

		return entityLstRespone;

	}

	private Object getEntityRegex(Session session, Ku ku, Long id) {

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<EntityRegex> select = builder.createQuery(EntityRegex.class);
		Root<EntityRegex> root = select.from(EntityRegex.class);
		ParameterExpression<Long> kuId = builder.parameter(Long.class);
		Predicate kuIdP = builder.equal(root.get("entityId"), kuId);
		Predicate and1 = builder.and(kuIdP);
		select.where(and1);

		List<EntityRegex> entityRegexDetails = session.createQuery(select).setParameter(kuId, id).getResultList();

		return entityRegexDetails;
	}

	private Object getRegex(Session session, Ku ku, Long id) {

		RegEx regex = null;

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<RegEx> select = builder.createQuery(RegEx.class);
		Root<RegEx> root = select.from(RegEx.class);
		ParameterExpression<Long> kuId = builder.parameter(Long.class);
		Predicate kuIdP = builder.equal(root.get("id"), kuId);
		Predicate and1 = builder.and(kuIdP);
		select.where(and1);

		List<RegEx> regexDetails = session.createQuery(select).setParameter(kuId, id).getResultList();

		regex = regexDetails.get(0);
		return regex;
	}

}
