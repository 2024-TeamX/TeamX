package com.teamx.exsite.controller.customercenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.teamx.exsite.model.dto.user.UserDTO;
import com.teamx.exsite.model.vo.customercenter.Inquiry;
import com.teamx.exsite.service.customercenter.CustomercenterService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CustomercenterController {
	
	private final CustomercenterService customercenterService;
	
	@Autowired
	public CustomercenterController(CustomercenterService customercenterService) {
		
		this.customercenterService = customercenterService;
	
	}
	
	@GetMapping("/customer/service")
	public String customerService(@RequestParam(required = false) Integer userNo
								, Model model
								, HttpSession session
								, @RequestParam(required = false) String personalInquiery) {
		
	    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
	 
	    model.addAttribute("loginUser", loginUser);
	    model.addAttribute("personalInquiery", personalInquiery);
	     
	    if (loginUser != null) {
	    	
	    	List<Inquiry> inquiries = customercenterService.getInquiriesByUserNo(loginUser.getUserNo());
	        model.addAttribute("inquiries", inquiries);
	    
	    }

	    return "customercenter/customerService";
	    
	}
	
	@GetMapping("/customer/inquiry")
	public String showInquiryForm() {
		
		return "customercenter/inquiry";
		
	}
	
	@GetMapping("/customer/answer")
	public String inquiryAnswer (@RequestParam("inquiryNo") int inquiryNo, Model model, HttpSession session) {
		
		UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
	    
	    if (loginUser == null) {
	        return "redirect:login";
	    }

	    Inquiry inquiryDetails = customercenterService.getInquiryById(inquiryNo);
	    
	    if (inquiryDetails.getUserNo() != loginUser.getUserNo()) {
	        return "redirect:customer/service"; // 다른 유저의 문의에 접근할 경우 리다이렉트
	    }
	    
		model.addAttribute("inquiry", inquiryDetails);
		
		return "customercenter/inquiryAnswer";
		
	}
	
	@PostMapping("/customer/inquiry/create")
	public String createInquiry (@RequestParam String inquiryTitle, @RequestParam String inquiryContent, HttpSession session) {
		
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		
		if (loginUser == null) {
	        return "redirect:login";
	    }
		
		Inquiry inquiry = new Inquiry();
		inquiry.setUserNo(loginUser.getUserNo());
		inquiry.setInquiryTitle(inquiryTitle);
		inquiry.setInquiryContent(inquiryContent);
		
		customercenterService.saveInquiry(inquiry);
		
		return "redirect:customer/service";
	
	}
	
	@GetMapping("/customer/deleteInquiry")
    public String deleteInquiry(@RequestParam("inquiryNo") int inquiryNo, HttpSession session) {
		
		 UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
		    
		    if (loginUser == null) {
		        return "redirect:login";
		    }
		    
		    Inquiry inquiry = customercenterService.getInquiryById(inquiryNo);
		    
		    if (inquiry.getUserNo() == loginUser.getUserNo()) {
		    	customercenterService.deleteInquiry(inquiryNo);
		    }
        
        return "redirect:customer/service"; 
        
	}
	
	// 관리자 페이지 1:1 문의목록 조회 
	@ResponseBody
	@GetMapping("/api/inquiries")
	public List<Inquiry> getInquiries() {
		
        return customercenterService.getAllInquiries();
    
	}
	
	// 관리자 페이지 해당 1:1문의 답변하기 페이지 조회
	@ResponseBody
	@GetMapping("/api/inquiries/{inquiryNo}")
	public Inquiry getInquiryAnswerById(@PathVariable int inquiryNo) {
		
        return customercenterService.getInquiryById(inquiryNo);
    
	}
	
	// 관리자 페이지 1:1문의 특정회원 검색하기
	@ResponseBody
	@GetMapping("/api/inquiries/search")
	public List<Inquiry> searchUsers(@RequestParam String userId) {
		
		return customercenterService.searchUsers(userId) ;
			 		 
	}
	
	// 관리자 페이지 관리자 답변 등록
	@ResponseBody
	@PutMapping("/api/inquiries/save/{inquiryNo}")
    public ResponseEntity<Inquiry> saveInquiryResponse(@PathVariable int inquiryNo, @RequestBody Inquiry inquiry) {
		
		Inquiry updateInquiry = customercenterService.updateInquiry(inquiryNo, inquiry);
        
		return ResponseEntity.ok(updateInquiry);
        
    }
	
}
