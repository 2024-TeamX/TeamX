package com.teamx.exsite.controller.exmap;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.teamx.exsite.model.vo.exmap.ExmapCoordinate;
import com.teamx.exsite.model.vo.exmap.ExmapExhibition;
import com.teamx.exsite.service.exmap.ExmapService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
//@Slf4j
public class ExmapController {

	
	private final ExmapService exmapService;
	

	@GetMapping("/exmap")
	public String exmap(Model model) {
		
		List<String> gunameList = exmapService.getDistinctGunameList();
	    model.addAttribute("gunameList", gunameList);
		return "exmap/exmap";
	}
	
	/**
	 * 유저가 선택한 지역구명을 기반으로 해당 지역의 전시장소 좌표를 조회/반환
	 * @param guname 지역구명
	 * @return 좌표
	 */
	@ResponseBody
	@GetMapping("/exmap/coordinates")
	public List<ExmapCoordinate> selectAllCoordinates(@RequestParam String guname) {
//		System.out.println(guname);
		return exmapService.selectAllExmapCoordinates(guname);  
	}
	
	
	@GetMapping("/exmap/exhibition/info")
	@ResponseBody
	public ExmapExhibition getExhibitionInfo(@RequestParam("lat") String lat, 
	                                        @RequestParam("lot") String lot) {
	    return exmapService.getRecentExhibitionInfo(lat, lot);
	}
	
	
}
