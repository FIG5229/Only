/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.thinkgem.jeesite.modules.affair.web;

import com.thinkgem.jeesite.common.beanvalidator.BeanValidators;
import com.thinkgem.jeesite.common.config.Global;
import com.thinkgem.jeesite.common.persistence.Page;
import com.thinkgem.jeesite.common.utils.Encodes;
import com.thinkgem.jeesite.common.utils.StringUtils;
import com.thinkgem.jeesite.common.utils.excel.ExportExcelNew;
import com.thinkgem.jeesite.common.utils.excel.ImportExcel;
import com.thinkgem.jeesite.common.vo.Result;
import com.thinkgem.jeesite.common.web.BaseController;
import com.thinkgem.jeesite.common.web.UploadController;
import com.thinkgem.jeesite.modules.affair.entity.AffairPolicewomanTalent;
import com.thinkgem.jeesite.modules.affair.service.AffairPolicewomanTalentService;
import com.thinkgem.jeesite.modules.cms.entity.Article;
import com.thinkgem.jeesite.modules.cms.entity.ArticleData;
import com.thinkgem.jeesite.modules.cms.entity.Category;
import com.thinkgem.jeesite.modules.personnel.service.PersonnelBaseService;
import com.thinkgem.jeesite.modules.sys.service.OfficeService;
import com.thinkgem.jeesite.modules.sys.service.SystemService;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 各类特长人才Controller
 * @author cecil.li
 * @version 2019-11-05
 */
@Controller
@RequestMapping(value = "${adminPath}/affair/affairPolicewomanTalent")
public class AffairPolicewomanTalentController extends BaseController {

	@Autowired
	private AffairPolicewomanTalentService affairPolicewomanTalentService;

	@Autowired
	private UploadController uploadController;

	@Autowired
	private OfficeService officeService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private PersonnelBaseService personnelBaseService;
	
	@ModelAttribute
	public AffairPolicewomanTalent get(@RequestParam(required=false) String id) {
		AffairPolicewomanTalent entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = affairPolicewomanTalentService.get(id);
		}
		if (entity == null){
			entity = new AffairPolicewomanTalent();
		}
		return entity;
	}
	
	@RequiresPermissions("affair:affairPolicewomanTalent:view")
	@RequestMapping(value = {"list", ""})
	public String list(AffairPolicewomanTalent affairPolicewomanTalent, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<AffairPolicewomanTalent> page = affairPolicewomanTalentService.findPage(new Page<AffairPolicewomanTalent>(request, response), affairPolicewomanTalent); 
		model.addAttribute("page", page);
		return "modules/affair/affairPolicewomanTalentList";
	}

	@RequiresPermissions("affair:affairPolicewomanTalent:view")
	@RequestMapping(value = "form")
	public String form(AffairPolicewomanTalent affairPolicewomanTalent, Model model) {
		model.addAttribute("affairPolicewomanTalent", affairPolicewomanTalent);
		return "modules/affair/affairPolicewomanTalentForm";
	}

	@RequiresPermissions("affair:affairPolicewomanTalent:edit")
	@RequestMapping(value = "save")
	public String save(AffairPolicewomanTalent affairPolicewomanTalent, Model model, RedirectAttributes redirectAttributes,HttpServletRequest request) {
		if (!beanValidator(model, affairPolicewomanTalent)){
			return form(affairPolicewomanTalent, model);
		}
		/*int Date = Calendar.getInstance().get(Calendar.DATE);
		if (request.getParameter("unitId")!=null||!"".equals(request.getParameter("unitId"))){
			User user = systemService.getUser(request.getParameter("unitId"));

			PersonnelBase personnelBase = personnelBaseService.findInfoByIdNumber(user.getNo());

			*//*int age = Date-personnelBase.getBirthday();*//*
			affairPolicewomanTalent.setUnit(personnelBase.getWorkunitName());
			affairPolicewomanTalent.setUnitId(personnelBase.getWorkunitId());
			affairPolicewomanTalent.setName(personnelBase.getName());
			affairPolicewomanTalent.setBirthday(personnelBase.getBirthday());
			*//*affairPolicewomanTalent.setAge(()/365);*//*
			affairPolicewomanTalent.setJob(personnelBase.getJobAbbreviation());
			affairPolicewomanTalentService.save(affairPolicewomanTalent);

		}*/
		affairPolicewomanTalentService.save(affairPolicewomanTalent);
		addMessage(redirectAttributes, "保存女警风采信息成功");
		request.setAttribute("saveResult","success");
		return "modules/affair/affairPolicewomanTalentForm";
	}

	@RequiresPermissions("affair:affairPolicewomanTalent:edit")
	@RequestMapping(value = "delete")
	public String delete(AffairPolicewomanTalent affairPolicewomanTalent, RedirectAttributes redirectAttributes) {
		affairPolicewomanTalentService.delete(affairPolicewomanTalent);
		addMessage(redirectAttributes, "删除女警风采信息成功");
		return "redirect:"+Global.getAdminPath()+"/affair/affairPolicewomanTalent/?repage";
	}

	/**
	 * 详情
	 * @param affairPolicewomanTalent
	 * @param model
	 * @return
	 */
	@RequiresPermissions("affair:affairPolicewomanTalent:view")
	@RequestMapping(value = "formDetail")
	public String formDetail(AffairPolicewomanTalent affairPolicewomanTalent, Model model) {
		model.addAttribute("affairPolicewomanTalent", affairPolicewomanTalent);
		if (affairPolicewomanTalent.getFilePath() != null && affairPolicewomanTalent.getFilePath().length() > 0) {
			List<Map<String, String>> filePathList = uploadController.filePathHandle(affairPolicewomanTalent.getFilePath());
			model.addAttribute("filePathList", filePathList);
		}
		return "modules/affair/affairPolicewomanTalentFormDetail";
	}

	@ResponseBody
	@RequiresPermissions("affair:affairPolicewomanTalent:edit")
	@RequestMapping(value = {"deleteByIds"})
	public Result deleteByIds(@RequestParam("ids[]") List<String> ids) {
		Result result = new Result();
		if(ids != null && ids.size() > 0){
			affairPolicewomanTalentService.deleteByIds(ids);
			result.setSuccess(true);
			result.setMessage("删除成功");
		}else{
			result.setSuccess(false);
			result.setMessage("请先选择要删除的内容");
		}
		return result;
	}
	/**
	 * 导出excel格式数据
	 * @param
	 * @param request
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "export", method= RequestMethod.POST)
	public String exportExcelByTemplate(AffairPolicewomanTalent affairPolicewomanTalent, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, boolean flag) {
		XSSFWorkbook wb = null;
		try
		{
			String fileName = "";
			if(request.getParameter("fileName")!=null && !"".equals(request.getParameter("fileName"))){
				fileName= request.getParameter("fileName").toString();
			}

			Page<AffairPolicewomanTalent> page = null;
			if(flag == true){
				page = affairPolicewomanTalentService.findPage(new Page<AffairPolicewomanTalent>(request, response), affairPolicewomanTalent);
			}else{
				page = affairPolicewomanTalentService.findPage(new Page<AffairPolicewomanTalent>(request, response,-1), affairPolicewomanTalent);
			}

			String fileSeperator = File.separator;
			String filePath= Global.getUserfilesBaseDir()+fileSeperator+"userfiles"+fileSeperator+"template"+fileSeperator;
			InputStream inputStream = new FileInputStream(filePath+fileName);
			if (null != inputStream)
			{
				try
				{
					wb = new  XSSFWorkbook(inputStream);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			ExportExcelNew exportExcelNew = new ExportExcelNew(0, AffairPolicewomanTalent.class);
			exportExcelNew.setWb(wb);
			List<AffairPolicewomanTalent> list =page.getList();
			exportExcelNew.setDataList(list);
			HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
			response.reset();
			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename="+ Encodes.urlEncode(fileName));
			ServletOutputStream fout = response.getOutputStream();
			wb.write(fout);
			fout.close();
			return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			addMessage(redirectAttributes, "导出用户失败！失败信息："+ex);
		}
		return "redirect:" + adminPath + "/personnel/affairPolicewomanTalent/?repage";
	}


	@RequestMapping(value = "import", method= RequestMethod.POST)
	public String importFile(MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			int successNum = 0;
			int failureNum = 0;
			StringBuilder failureMsg = new StringBuilder();
			ImportExcel ei = new ImportExcel(file, 0, 0);
			List<AffairPolicewomanTalent> list = ei.getDataList(AffairPolicewomanTalent.class);
			for (AffairPolicewomanTalent affairPolicewomanTalent : list){
				try{
					BeanValidators.validateWithException(validator, affairPolicewomanTalent);
					//绑定单位对应的机构id
					affairPolicewomanTalent.setUnitId(officeService.findByName(affairPolicewomanTalent.getUnit()));
					affairPolicewomanTalentService.save(affairPolicewomanTalent);
					successNum++;
				}catch(ConstraintViolationException ex){
					List<String> messageList = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
					for (String message : messageList){
						failureMsg.append(message+"; ");
						failureNum++;
					}
				}catch (Exception ex) {
					failureMsg.append(" 导入失败："+ex.getMessage());
				}
			}
			if (failureNum>0){
				failureMsg.insert(0, "，失败 "+failureNum+" 条，导入信息如下：");
			}
			addMessage(redirectAttributes, "已成功导入 "+successNum+" 条"+failureMsg);
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入失败！失败信息："+e.getMessage());
		}
		redirectAttributes.addFlashAttribute("result","success");
		return "redirect:" + adminPath + "/file/template/download/view";
	}

	/*
	 * 推送页面
	 * @param affairPolicewomanTalent
	 * @param model
	 * @return
	 * */
	@RequiresPermissions("affair:affairPolicewomanTalent:edit")
	@RequestMapping(value = "propelling")
	public String propelling(AffairPolicewomanTalent affairPolicewomanTalent, Model model) {
		//栏目
		Category category = new Category();
		category.setId(affairPolicewomanTalent.getId());
		//文章副表
		ArticleData articleData = new ArticleData();
		//内容-->作品内容
		articleData.setContent(affairPolicewomanTalent.getContent());
		//创建文章Entity
		Article article = new Article();
		article.setCategory(category);
		//设置文章副表
		article.setArticleData(articleData);
		//标题
		article.setTitle(affairPolicewomanTalent.getJob());
		//创建时间
		article.setCreateDate(affairPolicewomanTalent.getCreateDate());
		//附件
		article.setAppendfile("/politics"+affairPolicewomanTalent.getFilePath());
		model.addAttribute("article", article);
		return "modules/affair/affairPolicewomanTalentPropelling";
	}
}