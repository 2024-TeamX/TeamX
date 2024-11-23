package com.teamx.exsite.controller.review;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.teamx.exsite.model.dto.user.UserDTO;
import com.teamx.exsite.model.vo.review.ReviewDTO;
import com.teamx.exsite.service.review.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;
	
	@ResponseBody
	@GetMapping("/review/write")
	public ResponseEntity<String> reviewWrite(HttpSession session, Model model) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰 작성은 로그인해야 가능합니다.");
		}
		
		// 예마한 전시 중 관람일이 지난 것들
		List<ReviewDTO> viewedContent = reviewService.selelectViewedContent(loginUser.getUserNo());
		//작성한 리뷰들
		List<ReviewDTO> writedReview = reviewService.selectWritedReviewList(loginUser.getUserNo());
		
		List<ReviewDTO> filteredViewedContent = null;
		
		if (viewedContent == null) {
			// 예매하거나 관람일이 지난 전시가 없다면 작성가능한 리뷰가 없다, 예매 여부는 상세조회 시 체크하므로 여기서는 관람일 지난 예매가 없는 것
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰작성은 관람일 이후에 가능합니다.");
		}
		
		return ResponseEntity.ok("리뷰 작성이 가능합니다.");
	}
	
	@GetMapping("/review/write/form")
	public String reviewWriteForm(HttpSession session, Model model) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		if(loginUser == null) {
			return "login/loginForm";
		}
		// 예마한 전시 중 관람일이 지난 것들
		List<ReviewDTO> viewedContent = reviewService.selelectViewedContent(loginUser.getUserNo());
		//작성한 리뷰들
		List<ReviewDTO> writedReview = reviewService.selectWritedReviewList(loginUser.getUserNo());
		//위에 두 개를 필터링
		List<ReviewDTO> filteredViewedContent = reviewService.writingPossibleReviews(viewedContent, writedReview);
		
		model.addAttribute("viewdContent", filteredViewedContent);
		return "review/writeReview";
	}
	
	
	@ResponseBody
	@PostMapping("/review/write/confirm")
	public ResponseEntity<String> reviewWriteConfirm(HttpSession session, ReviewDTO review) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인한 상태에서 작성 가능합니다.");
		}
		
		review.setUserNo(loginUser.getUserNo());
		
		if(review.getExhibitionTitle() == null || review.getExhibitionTitle().trim().equals("")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰를 작성할 전시회를 선택하세요.");
		} else if(review.getReviewTitle() == null || review.getReviewTitle().trim().equals("")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("제목을 입력하세요.");
		} else if(review.getReviewContent() == null || review.getReviewContent().trim().equals("")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("내용을 입력하세요.");
		}
		// 개행상태를 유지
		review.setReviewContent(review.getReviewContent().replace("\n", "<br>"));
		
		int result = reviewService.insertReview(review);
		
		if(result == 1) {
			return ResponseEntity.ok("리뷰가 게시되었습니다.");
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버에 오류가 있습니다. 다시 시도해주세요.");
		}
	}
	
	@GetMapping("/review/replace/form")
	public String reviewReplaceForm() {
		return "review/replaceReview";
	}
	
	@ResponseBody
	@PostMapping("/review/replace/confirm")
	public ResponseEntity<String> reviewReplaceConfirm(HttpSession session, ReviewDTO replaceReview) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인한 상태에서 수정 가능합니다.");
		}
		
		replaceReview.setUserNo(loginUser.getUserNo());
		
		if(replaceReview.getReviewTitle() == null ||replaceReview.getReviewTitle().trim().equals("")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("제목을 입력하세요.");
		} else if(replaceReview.getReviewContent() == null || replaceReview.getReviewContent().trim().equals("")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("내용을 입력하세요.");
		}
		
		// 개행상태를 유지
		replaceReview.setReviewContent(replaceReview.getReviewContent().replace("\n", "<br>"));
		
		int result = reviewService.updateReview(replaceReview);

		if(result == 1) {
			return ResponseEntity.ok("리뷰가 수정되었습니다.");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰 수정에 실패했습니다.");
		}
	}
	
	@ResponseBody
	@PutMapping("/review/delete")
	public ResponseEntity<String> deleteReview(HttpSession session, ReviewDTO deleteReview) {
		
		int result = reviewService.deleteReview(deleteReview.getMerchantUid());
		
		if(result == 1) {
			return ResponseEntity.ok("리뷰가 삭제되었습니다.");
		}
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body("리뷰 삭제에 오류가 발생했습니다.");
	}
	
	@ResponseBody
	@GetMapping(value="/api/review/info", produces="application/json; charset=utf-8")
	public ResponseEntity<List<ReviewDTO>> getReviewInfo() {
		
		List<ReviewDTO> list = reviewService.getReviewList();
		
		log.info("{}", list);
		
		return ResponseEntity.ok(list);
	}
	
	@ResponseBody
	@PutMapping(value="/api/review/update", produces="application/json; charset=utf-8")
	public ResponseEntity<String> deleteReview(@RequestBody Map<String, List<String>> deleteReviews) {
		
		log.info("{}", deleteReviews);
		
		List<String> list = deleteReviews.get("deleteReview");
		
		for(int i = 0; i < list.size(); i++) {
			reviewService.deleteReview(list.get(i));
		}
		
		return ResponseEntity.ok("요청한 리뷰가 삭제되었습니다.");
	}
	
	
		
}
