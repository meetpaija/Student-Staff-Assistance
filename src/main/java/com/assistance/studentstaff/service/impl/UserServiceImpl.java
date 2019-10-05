package com.assistance.studentstaff.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.assistance.studentstaff.common.CustomGenericException;
import com.assistance.studentstaff.common.PasswordHashing;
import com.assistance.studentstaff.model.UserModel;
import com.assistance.studentstaff.model.UserRoleModel;
import com.assistance.studentstaff.repo.IUserRepo;
import com.assistance.studentstaff.repo.IUserRolesRepo;
import com.assistance.studentstaff.service.IDepartmentService;
import com.assistance.studentstaff.service.IProgramService;
import com.assistance.studentstaff.service.IUserAvatarImageService;
import com.assistance.studentstaff.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	IUserRepo userRepo;
	
	@Autowired
	JavaMailSender javaMailSender;

	@Autowired
	IUserRolesRepo userRolesRepo;

	@Autowired
	IUserAvatarImageService userAvatarImageService;
	
	@Autowired
	IDepartmentService departmentService;
	
	@Autowired
	IProgramService programService;

	@Override
	public List<UserModel> fetchAllUsers() {
		List<UserModel> users = userRepo.findAll();
		return users.stream().map(user -> setNullPassword(user)).collect(Collectors.toList());
	}

	private UserModel setNullPassword(UserModel user) {
		user.setPassword(null);
		return user;
	}

	@Override
	public UserModel insertUser(UserModel user) throws CustomGenericException {
		Optional<UserModel> existingUser = userRepo.findByEmailIdOrUserName(user.getEmailId(), user.getUserName());
		if (existingUser.isPresent()) {
			throw new CustomGenericException("User already exists");
		}
		Optional<UserRoleModel> userRole = userRolesRepo.findById(user.getRoleId());
		if (userRole.isPresent()) {
			if(user.getDeptId()!=null)departmentService.findDeptById(user.getDeptId());
			if(user.getProgId()!=null)programService.findProgramById(user.getProgId());
			user.setUserId(UUID.randomUUID().toString());
			user.setPassword(PasswordHashing.encrypt(user.getPassword()));
			return setNullPassword(userRepo.save(user));
		} else {
			throw new CustomGenericException("This role doesn't exists");
		}
	}

	@Override
	public UserModel findById(String userId) throws CustomGenericException {
		Optional<UserModel> user = userRepo.findById(userId);
		if (user.isPresent()) {
			return setNullPassword(user.get());
		} else {
			throw new CustomGenericException("User doesn't exist");
		}
	}

	@Override
	public UserModel updateUser(String userId, UserModel user) throws CustomGenericException {
		Optional<UserModel> existingUser = userRepo.findById(userId);
		if (existingUser.isPresent()) {
			user.setRoleId(existingUser.get().getRoleId());
			user.setUserId(userId);
			user.setPassword(existingUser.get().getPassword());
			return setNullPassword(userRepo.save(user));
		} else {
			throw new CustomGenericException("User doesn't exists");
		}
	}

	@Override
	public void deleteUser(String userId) throws CustomGenericException {
		Optional<UserModel> existingUser = userRepo.findById(userId);
		if (existingUser.isPresent()) {
			userRepo.deleteById(userId);
		} else {
			throw new CustomGenericException("User doesn't exists");
		}
	}

	@Override
	public UserModel loginUser(String userNameOrEmailId, String password) throws CustomGenericException {
		if(StringUtils.isNotEmpty(userNameOrEmailId) && StringUtils.isNotEmpty(password)) {
			Optional<UserModel> user = userRepo.findByEmailIdOrUserName(userNameOrEmailId, userNameOrEmailId);
			if(user.isPresent()) {
				if(StringUtils.equals(PasswordHashing.encrypt(password), user.get().getPassword())) {
					return user.get();
				} else {
					throw new CustomGenericException("You have entered wrong password");
				}
			} else {
				throw new CustomGenericException("You have entered wrong username/emailId");
			}
		} else {
			throw new CustomGenericException("Please correct username/email and password");
		}
	}

	@Override
	public UserModel changePassword(String userId, String newPassword) throws CustomGenericException {
		Optional<UserModel> existingUser = userRepo.findById(userId);
		if (existingUser.isPresent()) {
			existingUser.get().setPassword(PasswordHashing.encrypt(newPassword));
			return setNullPassword(userRepo.save(existingUser.get()));
		} else {
			throw new CustomGenericException("User doesn't exists");
		}
	}

	@Override
	public void forgetPassword(String emailId) throws CustomGenericException {
		Optional<UserModel> existingUser = userRepo.findByEmailIdOrUserName(emailId, emailId);
		if (existingUser.isPresent()) {
			String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
			String randomPwd = RandomStringUtils.random( 5, characters );
			existingUser.get().setPassword(PasswordHashing.encrypt(randomPwd));
			userRepo.save(existingUser.get());
			sendRandomPwdInMail(existingUser.get(), randomPwd);
		} else {
			throw new CustomGenericException("User doesn't exists");
		}
	}

	private void sendRandomPwdInMail(UserModel userModel, String randomPwd) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper;
		
		try {
			helper = new MimeMessageHelper(mimeMessage,true);
			helper.setTo(userModel.getEmailId());
			helper.setSubject("Password To Login Securely");
			helper.setText("Dear " + userModel.getUserName() + ",\n\n" 
						+ "Your random password for next login is :" + randomPwd + "\n\n" 
						+ "Please change your password after login to the System. \n\n" 
						+ "Thanks,\nStudent Staff Assistance System");
			javaMailSender.send(mimeMessage);
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
