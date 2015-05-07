package net.flexberry.services.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.flexberry.services.server.data.OdataHibernateDataProvider;
import net.flexberry.services.server.edmprovider.OdataHibernateEdmProvider;
import net.flexberry.services.server.processor.OdataHibernateProcessor;
import net.flexberry.services.util.HibernateUtil;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OdataHibernateServlet extends HttpServlet {

	  private static final long serialVersionUID = 1L;
	  private static final Logger LOG = LoggerFactory.getLogger(OdataHibernateServlet.class);

	  @Override
	  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
	      throws ServletException, IOException {
	    try {
	      HttpSession session = req.getSession(true);
	      OdataHibernateDataProvider dataProvider = (OdataHibernateDataProvider) session.getAttribute(OdataHibernateDataProvider.class.getName());
	      //HibernateUtil.testHibernate();
	      if (dataProvider == null) {
	        dataProvider = new OdataHibernateDataProvider(HibernateUtil.getClassesMapping());
	        session.setAttribute(OdataHibernateDataProvider.class.getName(), dataProvider);
	        LOG.info("Created new data provider.");
	      }
	      OData odata = OData.newInstance();
	      ServiceMetadata edm = odata.createServiceMetadata(new OdataHibernateEdmProvider(dataProvider), new ArrayList<EdmxReference>());
	      ODataHttpHandler handler = odata.createHandler(edm);
	      handler.register(new OdataHibernateProcessor(dataProvider));
	      handler.process(req, resp);
	    } catch (Exception e) {
	      LOG.error("Server Error", e);
	      throw new ServletException(e);
	    }
	  }
} 
