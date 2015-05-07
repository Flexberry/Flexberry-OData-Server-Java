package net.flexberry.services.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Value;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;


public class HibernateUtil {
  private static org.hibernate.SessionFactory sessionFactory = null;
  
  static {
      try {
              //creates the session factory from hibernate.cfg.xml
              sessionFactory = new Configuration().configure().buildSessionFactory();
      } catch (Exception e) {
            e.printStackTrace();
      }
  }

  public static org.hibernate.SessionFactory getSessionFactory() {
      return sessionFactory;
  }
  
  public static void testHibernate() {
    List objs=null;
    Session session = null;
    try {
      session = getSessionFactory().openSession();
      objs=session.createCriteria("servicebus.ApplicationLog").list();
    } finally {
        if (session != null && session.isOpen()) {
          session.close();
        }
    } 
  }
  
  
  public static List<String> getClassesMapping() throws Exception {
    Strategy strategy=new Strategy() {
      @Override
      public boolean write(Type type, Object value, NodeMap<OutputNode> node, Map map) throws Exception 
        { return false; }
      @Override
      public Value read(Type type, NodeMap<InputNode> node, Map map)throws Exception 
        { return null; }
    };
    
    Serializer serializer = new Persister(strategy);
    URL fileUrl = HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml");
    HibernateConfiguration conf = serializer.read(HibernateConfiguration.class, fileUrl.openStream());
    ArrayList<String> list=new ArrayList<String>();
    for (Mapping map : conf.sessionFactory.get(0).mappings) {
      list.add(map.className);
    }
    return list;
}
  
  
  
  @Root(strict=false,name="hibernate-configuration")
  public static class HibernateConfiguration {
    @ElementList(inline=true, required=false)
    public List<SessionFactory> sessionFactory;
  }

  @Root(strict=false,name="session-factory")
  public static class SessionFactory {
    @ElementList(entry="property", inline=true, required=false)
    private List<Property> properties;

    @ElementList(entry="mapping", inline=true, required=false)
    private List<Mapping> mappings;
    
  }

  @Root(strict=false)
  public static class Property {
    @Attribute(name="name")
    public String name;            
    @Text
    public String text;  
  }
  
  @Root(strict=false)
  public static class Mapping {
    @Attribute(name="class")
    public String className;            
  }

  
  
  
  public class MappingConverter implements Converter<Mapping> {

    public Mapping read(InputNode node) {
       //String name = node.getAttribute("name");
       //String value = node.getAttribute("value");

       return new Mapping();
    }

    public void write(OutputNode node, Mapping external) {
       //String name = external.getName();
       //String value = external.getValue();

       //node.setAttribute("name", name);
       //node.setAttribute("value", value);
    }
 }  
  
  
  
  
  
  
  
  
}






/*
public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory(
			    new StandardServiceRegistryBuilder().build() );
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
*/