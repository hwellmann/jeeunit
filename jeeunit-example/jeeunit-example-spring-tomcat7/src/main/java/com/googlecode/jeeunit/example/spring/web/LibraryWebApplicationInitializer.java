package com.googlecode.jeeunit.example.spring.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class LibraryWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext sc) throws ServletException {

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(WebSpringConfig.class);
        sc.addListener(new ContextLoaderListener(rootContext));
        
        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.setParent(rootContext);
        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcher = sc.addServlet("dispatcher",
                new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(2);
        dispatcher.addMapping("*.html");
        dispatcher.addMapping("*.form");
        dispatcher.addMapping("*.ajax");
    }

}
