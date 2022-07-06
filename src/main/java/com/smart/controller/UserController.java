package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	
	//method for adding common data for response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName=principal.getName();
	//	System.out.println("USERNAME "+userName);
		
		//get the user using username(Email)
		User user = this.userRepository.getUserByUserName(userName);
	//	System.out.println(user);
		
		model.addAttribute("user", user);
	}
	
	//dashbord home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {		//principal give a user unique id and then we get all data from our login form
		model.addAttribute("title","User Dashbord");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());	//send blank object 
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, Principal principal) {
		String name=principal.getName();				//return email id of User
			System.out.println(name);	
		User user = this.userRepository.getUserByUserName(name);
		contact.setUser(user);
		
		user.getContacts().add(contact);			//save contact in contact list which is in user class
		this.userRepository.save(user);				//and save data in database
		
		//System.out.println("Contact "+contact);
		System.out.println("Added to data base ");
		return "normal/add_contact_form";
	}
}
