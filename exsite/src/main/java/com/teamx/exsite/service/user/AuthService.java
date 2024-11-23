package com.teamx.exsite.service.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.teamx.exsite.model.dto.user.UserDTO;
import com.teamx.exsite.model.mapper.user.UserMapper;
import com.teamx.exsite.model.vo.user.VerificationInfo;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class AuthService {
	
	@Value("${message.fromNumber}")
	private String fromNumber;
	@Value("${spring.mail.username}")
	private String fromEmail;
	
	private final JavaMailSenderImpl mailSender;
	private Map<String, VerificationInfo> verificationCodes = new HashMap<>();
	private final UserMapper userMapper;
	private final DefaultMessageService messageService;
	private final PasswordEncoder passwordEncoder;
	
	
	// 회원 가입용 휴대폰 인증
	public void sendSMS(String phone) {
		Message message = new Message();
		String code = generateAuthCode();
		message.setFrom(fromNumber);
		message.setTo(phone);
		message.setText("[EX-SITE] 본인확인 인증번호는" + code + "입니다.");
		log.info("fromNumber: {}", fromNumber);
		messageService.sendOne(new SingleMessageSendingRequest(message));
		generateAuthInfo(phone, code);
	}
	
	public void sendSMS(String name, String phone) {
		Message message = new Message();
		String code = generateAuthCode();
		message.setFrom(fromNumber);
		message.setTo(phone);
		message.setText("[EX-SITE] 본인확인 인증번호는" + code + "입니다.");
		log.info("fromNumber: {}", fromNumber);
		messageService.sendOne(new SingleMessageSendingRequest(message));
		generateAuthInfo(name+phone, code);
	}
	
	//회원가입을 위한 이메일 인증
    public void javaMailSender(String mail) throws Exception {

        String code = generateAuthCode();
        MimeMessage mm = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
		helper.setSubject("[EX-SITE] 본인확인 인증번호"); // 메일 제목
		helper.setText(String.valueOf(code)); // 메일 내용
		helper.setFrom(fromEmail, "[EX-SITE] 본인확인 인증번호입니다."); // 발신자(이메일, 별칭)
		helper.setTo(mail);
		generateAuthInfo(mail, code);
		
		mailSender.send(mm);
    }
    
    // name+mail을 키값으로 인증정보 저장하고, 이메일로 인증번호 전달
    public void javaMailSender(String name, String mail) throws Exception {

        String code = generateAuthCode();
        MimeMessage mm = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
		helper.setSubject("[EX-SITE] 본인확인 인증 번호"); // 메일 제목
		helper.setText(String.valueOf(code)); // 메일 내용
		helper.setFrom(fromEmail, "[EX-SITE] 본인확인 인증번호입니다."); // 발신자(이메일, 별칭)
		helper.setTo(mail);
		generateAuthInfo(name+mail, code);
		
		mailSender.send(mm);
    }

    // 전달받은 두 값을 각각 key - value로 인증정보 vericationCodes 맵에서 일치하는 값을 찾아 결과 반환
    public Map<String, String> verifyCode(String key, String inputCode) {
    	LocalDateTime now = LocalDateTime.now();
        VerificationInfo storedCode = verificationCodes.get(key);
        Map<String, String> response = new HashMap<>();
    	
        if(now.isAfter(storedCode.getTimeStamp().plusMinutes(3))) {
        	 response.put("status", "timeout");
        	 verificationCodes.remove(key);
        	 return response;
        }
        if(storedCode.getCode() != null && storedCode.getCode().equals(inputCode)) {
        	response.put("status", "success");
        	verificationCodes.remove(key);
        	return response;
        }
        response.put("status", "false");
        return response;
    }
    
    public Map<String, String> processAuth(String value, String type) throws Exception {
        Map<String, String> response = new HashMap<>();
        int checkResult = ("email".equals(type)) ? mailCheck(value.trim()) : phoneCheck(value.trim());

        if (checkResult > 0) {
            response.put("status", "exist");
            return response;
        }

        if ("email".equals(type)) {
            javaMailSender(value);
        } else if ("phone".equals(type)) {
            sendSMS(value);
        }

        response.put("status", "ok");
        return response;
    }
    
    // 전달받은 값을 각각 key - value 값 쌍으로 인증정보 객체를 생성
    public void generateAuthInfo(String key, String value) {
    	this.verificationCodes.put(key, new VerificationInfo(value, LocalDateTime.now()));
    }
    
    // 6자리 랜덤 인증번호를 생성
    public String generateAuthCode() {
    	return String.valueOf(100000 + (int)(Math.random()*900000));
    }
    
	public int mailCheck(String email) {
		return userMapper.mailCheck(email);
	}

	public int phoneCheck(String phone) {
		return userMapper.phoneCheck(phone);
	}

	public boolean passwordCheck(String password, HttpSession session) {
		UserDTO loginUser = (UserDTO)session.getAttribute("loginUser");
		
		String passwordInDatabase = userMapper.getPassword(loginUser.getUserNo());
		
		return passwordEncoder.matches(password, passwordInDatabase);
		
	}
	
}
