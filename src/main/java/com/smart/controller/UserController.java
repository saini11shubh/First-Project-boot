package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data for response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		// System.out.println("USERNAME "+userName);

		// get the user using username(Email)
		User user = this.userRepository.getUserByUserName(userName);
		// System.out.println(user);

		model.addAttribute("user", user);
	}

	// dashbord home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) { // principal give a user unique id and then we get all
																// data from our login form
		model.addAttribute("title", "User Dashbord");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact()); // send blank object
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Principal principal) {
		try {
			String name = principal.getName(); // return email id of User
			System.out.println(name);

			// processing and uploading file
			if (file.isEmpty()) {
				// if the file is empty then try our message
				contact.setImage("contact.png");
				System.out.println("File is Empty");
			} else {
				// file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			User user = this.userRepository.getUserByUserName(name);
			contact.setUser(user);

			user.getContacts().add(contact); // save contact in contact list which is in user class
			this.userRepository.save(user); // and save data in database

			// System.out.println("Contact "+contact);
			System.out.println("Added to data base ");

			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

		} catch (Exception e) {
			// TODO: handle exception
			session.setAttribute("message", new Message("Something went wrong  !! Try again..", "danger"));
			System.out.println(e.getMessage());
		}
		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page on show contact =5
	// current page is start=0
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts");

		String userName = principal.getName(); // get a user id
		User user = this.userRepository.getUserByUserName(userName); // by user id we take a all user data

		// currentPage -page
		// Contact per page -5
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable); // in this we pass
																									// user id and show
																									// all user contacts
																									// list
		model.addAttribute("contacts", contacts); // we send data on view page
		model.addAttribute("currentPage", page); // we are on page now
		model.addAttribute("totalPages", contacts.getTotalPages()); // total pages
		return "normal/show_contacts";
	}

	@RequestMapping("show-contacts/{cid}/contact")
	public String showContactDetail(@PathVariable Integer cid, Model model, Principal principal) {
		System.out.println("CId " + cid);

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		// checking user id and contact id
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, Principal principal,
			HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		System.out.println("Contact Id " + cid);

		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		try {
			if (user.getId() == contact.getUser().getId()) {
				contact.setUser(null);
				this.contactRepository.delete(contact);
				session.setAttribute("Message", new Message("contact deleted successfully...", "success"));
				
				//delete file
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile,contact.getImage());
				file1.delete();
				System.out.println("Product Image Deleted !!!");
				
			}
		} catch (Exception e) {
			System.out.println("Image not delete");
			System.out.print(e.getMessage());
			// TODO: handle exception
		}

		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable Integer cid, Model model) {
		model.addAttribute("title","Update Contact");
		
		Optional<Contact> contactOptional= this.contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		model.addAttribute("contact",contact);
		System.out.println("Update contact");
		return "normal/update_form";
	}
	
	//update contact handler when we submit form 
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		//System.out.println("Contact Name "+ contact.getName());
		System.out.println("Id is "+contact.getCid());
		try {
			//old contact details
			Contact oldcontactDetail=this.contactRepository.findById(contact.getCid()).get();
			System.out.println("Process_update");
			//image
			if(!file.isEmpty()) {
				//delete file
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
				//update new file
				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldcontactDetail.getImage());
			}		
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated...","success"));
					}
		catch (Exception e) {
//			 TODO: handle exception
			session.setAttribute("message", new Message("Something went wrong!!...","error"));
			System.out.println("Something went wrong");
			e.printStackTrace();
		}		
		return "redirect:/user/show-contacts/"+contact.getCid()+"/contact";		
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title",	"Profile Page");
		return "normal/profile";
	}
	
	//for search 
	@GetMapping("/search")
	public String search(@RequestParam("keyword") String keyword, Model model,Contact contact) {
		List<Contact> contacts = this.contactRepository.findContactByName(keyword);
		model.addAttribute("Contacts", contacts);

		if (keyword != null) {
			List<Contact> list = this.contactRepository.findContactByName(keyword);
			model.addAttribute("list", list);
		} else {
			List<Contact> list =this.contactRepository.findAll();
			model.addAttribute("list", list);
		}

		return "/normal/show_contacts";
	}
}

