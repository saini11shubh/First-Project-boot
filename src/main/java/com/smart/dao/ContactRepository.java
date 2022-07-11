package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
	
			/* -----Pagination----*/
	//page->it is sublist of a list.It allows gain information about the position of it in the containing entire list.
	//pageable -> its have 2 information one current page and contact per page 
	@Query("from Contact as c where c.user.id=:userId")
	public Page<Contact> findContactByUser(@Param("userId")int userId, Pageable pageable);

	@Query("from Contact as c where c.name=:name")
	public List<Contact> findContactByName(@Param("name")String name);
	

}
