package org.apache.olingo.server.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.flexberry.services.util.HibernateUtil;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.apache.olingo.server.sample.data.StudentsDataProvider;
import org.apache.olingo.server.sample.edmprovider.StudentsEdmProvider;
import org.apache.olingo.server.sample.processor.StudentsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

public class StudentsServlet extends HttpServlet {

	  private static final long serialVersionUID = 1L;
	  private static final Logger LOG = LoggerFactory.getLogger(StudentsServlet.class);

	  @Override
	  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
	      throws ServletException, IOException {
	    try {
	      HttpSession session = req.getSession(true);
	      StudentsDataProvider dataProvider = (StudentsDataProvider) session.getAttribute(StudentsDataProvider.class.getName());
	      if (dataProvider == null) {
	        dataProvider = new StudentsDataProvider(HibernateUtil.getClassesMapping());
	        session.setAttribute(StudentsDataProvider.class.getName(), dataProvider);
	        LOG.info("Created new data provider.");
	      }
	      OData odata = OData.newInstance();
	      ServiceMetadata edm = odata.createServiceMetadata(new StudentsEdmProvider(dataProvider), new ArrayList<EdmxReference>());
	      ODataHttpHandler handler = odata.createHandler(edm);
	      handler.register(new StudentsProcessor(dataProvider));
	      handler.process(req, resp);
	    } catch (Exception e) {
	      LOG.error("Server Error", e);
	      throw new ServletException(e);
	    }
	  }
} 
