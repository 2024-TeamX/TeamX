package com.teamx.exsite.controller.review;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewController {


	@GetMapping("/review")
	public String review() {
		return "reviewPage/review"; 
	}
	@GetMapping("/reviewCheck")
	public String reviewCheck() {
		return "reviewPage/reviewCheck"; 
	}
	@GetMapping("/replaceReview")
	public String replaceReview() {
		return "reviewPage/replaceReview"; 
	}
}
