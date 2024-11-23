package com.teamx.exsite.controller.mypage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.teamx.exsite.common.model.vo.PageInfo;
import com.teamx.exsite.common.template.Pagination;
import com.teamx.exsite.model.dto.user.UserDTO;
import com.teamx.exsite.model.vo.exhibition.ExhibitionEvent;
import com.teamx.exsite.model.vo.review.ReviewDTO;
import com.teamx.exsite.model.vo.ticketing.PaymentDTO;
import com.teamx.exsite.service.mypage.MypageService;
import com.teamx.exsite.service.review.ReviewService;
import com.teamx.exsite.service.user.AuthService;
import com.teamx.exsite.service.user.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MypageController {
	
	private final AuthService authService;
	private final UserService userService;
	private final MypageService mypageService;
	private final ReviewService reviewService;

	@GetMapping("/mypage/main")
	public String mypageMain() {
		return "mypage/mypageMain";
	}
	
	@GetMapping("/mypage/view")
    public String mypageView(@RequestParam(value = "view", required = false) String view
    						, @RequestParam(value="cpage", defaultValue="1") int currentPage
    						, @RequestParam(value="ticketingDateRange", defaultValue="전체기간") String ticketingDateRange
    						, @RequestParam(value="merchantUid", required=false) String merchantUid
    						, @RequestParam(value="ticketingPrice", required=false) String ticektingPrice
    						, Model model
    						, HttpSession session) {
        // view 파라미터에 따라 상태값을 true로 설정
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return "user/loginForm";
		}
		PageInfo pageInfo = null;
        if (view != null) {
            switch (view) {
                case "modifyUserPasswordCheck":
                	if(loginUser.getMethod() != null) {
                		if(loginUser.getMethod().equals("NAVER") || loginUser.getMethod().equals("GOOGLE")) {
                    		model.addAttribute("showModifyUser", true);
                    		return "mypage/mypageView";
                    	}
                	}
                    model.addAttribute("showModifyUserPasswordCheck", true);
                    break;
                case "modifyUser":
                    model.addAttribute("showModifyUser", true);
                    break;
                case "passwordChange":
                    model.addAttribute("showPasswordChange", true);
                    break;
                case "ticketList":
                	int listCount = mypageService.selectTicketingListCount(loginUser.getUserNo(), ticketingDateRange);
            		pageInfo = Pagination.getPageInfo(listCount, currentPage, 10, 15);
            		List<PaymentDTO> ticketingInfo = mypageService.selectTicketingList(loginUser.getUserNo(), pageInfo, ticketingDateRange);
            		
            		model.addAttribute("showTicketList", true);
            		model.addAttribute("pageInfo", pageInfo);
            		model.addAttribute("ticketingInfo", ticketingInfo);
                	
                    model.addAttribute("showTicketList", true);
                    break;
                case "ticketDetail":
                	PaymentDTO paymentInfo = null;
                	
                	if(ticektingPrice != null ) {
                		paymentInfo = mypageService.selectTicketingInfo(merchantUid);
                	} else {
                		paymentInfo = mypageService.selectTicketingInfoNonePrice(merchantUid);
                	}
                	
                	model.addAttribute("paymentInfo", paymentInfo);
                	log.info("{}", paymentInfo);
                	model.addAttribute("pageInfo", pageInfo);
                    model.addAttribute("showTicketDetail", true);
                    break;
                case "likeList":
                	List<ExhibitionEvent> likeList = mypageService.selectLikeList(loginUser.getUserNo());
                	model.addAttribute("likeList", likeList);
                    model.addAttribute("showLikeList", true);
                    break;
                case "reviewList":
                	int reviewListCount = mypageService.selectMyPageReviewCount(loginUser.getUserNo());
            		pageInfo = Pagination.getPageInfo(reviewListCount, currentPage, 10, 15);
                	List<ReviewDTO> reviewList = reviewService.selectMyPageReviewList(loginUser.getUserNo(), pageInfo);
                	model.addAttribute("reviewList", reviewList);
                	log.info("{}", reviewList);
                	model.addAttribute("pageInfo", pageInfo);
                    model.addAttribute("showReviewList", true);
                    break;
                case "withdraw":
                    model.addAttribute("showWithdraw", true);
                    break;
                default:
                    break;
            }
        }
        return "mypage/mypageView";
    }
	
	@PostMapping("/mypage/verify/password")
	public String passwordCheck(String password, HttpSession session, Model model) {
		
		boolean isTrue = authService.passwordCheck(password, session);
		if(isTrue) {
			model.addAttribute("showModifyUser", true);
		} else {
			model.addAttribute("showModifyUserPasswordCheck", true);
			model.addAttribute("alertMsg", "비밀번호가 틀렸습니다.");
		}
		
		return "mypage/mypageView";
	}
	
	@PostMapping("/mypage/modify/personalinfo")
	public String modifyPersonalInfo(Model model, HttpSession session, UserDTO modifyInfo) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		modifyInfo.setUserNo(loginUser.getUserNo());
		modifyInfo.setSocialUserIdentifier(loginUser.getSocialUserIdentifier());
		log.info("{}", modifyInfo);
		UserDTO afterModifyInfo = loginUser.getMethod().equals("NORMAL") 
								 ? userService.normalUserModifyInfo(modifyInfo) 
								 : userService.socialUserModifyInfo(modifyInfo); 
		
		if(afterModifyInfo != null) {
			session.setAttribute("loginUser", afterModifyInfo);
		}
		
		model.addAttribute("showModifyUser", true);
		return "mypage/mypageView";
	}
	
	@PostMapping("/mypage/password/change")
	public String passwordchange(HttpSession session, Model model, String changePassword) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		if(!loginUser.getMethod().equals("NORMAL")) {
			model.addAttribute("alertMsg", "소셜 로그인한 회원은 비밀번호를 설정할 수 없습니다.");
			model.addAttribute("showPasswordChange", true);
			return "mypage/mypageView";
		} else {
			int result = userService.passwordChange(loginUser.getUserNo(), changePassword);
			if(result == 1) {
				model.addAttribute("alertMsg", "비밀번호가 변경되었습니다.");
			} else {
				model.addAttribute("alertMsg", "비밀번호가 변경에 실패했습니다.");
			}
		}
		model.addAttribute("showPasswordChange", true);
		return "mypage/mypageView";
	}
	
	@PostMapping("/mypage/withdraw")
	public String withdraw(HttpSession session, Model model, String userPw) {
		int userNo = ((UserDTO)session.getAttribute("loginUser")).getUserNo();
		
		int result = userService.withDrawUser(userNo, userPw);
		
		if(result == 1) {
			model.addAttribute("alertMsg", "회원탈퇴 처리되었습니다.");
			session.invalidate();
			return "common/main";
		} else {
			model.addAttribute("alertMsg", "회원탈퇴처리에 실패했습니다. 비밀번호를 다시 확인해주세요.");
			model.addAttribute("showWithdraw", true);
			return "mypage/mypageView";
		}
		
	}
	
	@ResponseBody
	@PostMapping(value="/mypage/withdraw/social/auth", produces="application/json; charset=utf-8;")
	public Map<String, String> withdrawSocialUserSendAuth(HttpSession session, Model model, String email) throws Exception {
		Map<String, String> result = new HashMap<>();
		
		int userNo = ((UserDTO)session.getAttribute("loginUser")).getUserNo();
		
		String authCode = authService.generateAuthCode();
		
		authService.generateAuthInfo(String.valueOf(userNo), authCode);
		
		if(authService.mailCheck(email) == 1) {
			authService.javaMailSender(email);
			result.put("status", "success");
		} else {
			result.put("status", "notfound");
		}
		
		return result;
	}
	
	@PostMapping("/mypage/withdraw/social/auth/verify")
	public String widdrawSocialUserAuthResult(HttpSession session, Model model, String email, String code) {
		Map<String, String> result = authService.verifyCode(email, code);
		int userNo = ((UserDTO)session.getAttribute("loginUser")).getUserNo();
		if(result.get("status").equals("success")) {
			userService.withDrawSocialUser(email, userNo);
			session.invalidate();
			return "redirect:/";
		} else if(result.get("status").equals("timeover")) {
			model.addAttribute("alertMsg", "인증유효시간이 초과되었습니다.");
			return "mypage/views/mypageWithdraw";
		} else {
			model.addAttribute("alertMsg", "인증 실패했습니다.");
			return "mypage/views/mypageWithdraw";
		}
	}
	
}
