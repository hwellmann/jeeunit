package com.googlecode.jeeunit.example.spring.web.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.googlecode.jeeunit.example.model.Book;
import com.googlecode.jeeunit.example.service.LibraryService;

@Controller
public class LibraryController {
    
    @Inject
    private LibraryService libraryService;
    

    @RequestMapping("/books.html")
    public ModelAndView showBooks() {
        libraryService.fillLibrary();
        
        List<Book> books = libraryService.findBooks();
        ModelAndView mav = new ModelAndView("books");
        mav.addObject("books", books);
        return mav;
    }
}
