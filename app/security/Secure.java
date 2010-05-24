package security;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Java;

import com.google.appengine.api.users.UserServiceFactory;

public class Secure extends Controller {

	@Before(unless = { "login", "authenticate", "logout" })
	static void checkAccess() throws Throwable {
		CheckIsAdmin check = getActionAnnotation(CheckIsAdmin.class);
		if (check != null) {
			checkIsAdmin(check);
		}
		check = getControllerInheritedAnnotation(CheckIsAdmin.class);
		if (check != null) {
			checkIsAdmin(check);
		}
	}

	private static void checkIsAdmin(CheckIsAdmin check) throws Throwable {
		if(!UserServiceFactory.getUserService().isUserLoggedIn()){
			Security.invoke("onCheckFailed");
		}
		
		if(!UserServiceFactory.getUserService().isUserAdmin()) {
			 Logger.warn("l'utilisateur courant n'est pas admin = %s", UserServiceFactory.getUserService().getCurrentUser().getEmail());	
             Security.invoke("onCheckFailed");
         }
	}

	public static class Security extends Controller {
		/**
		 * This method is called if a check does not succeed. By default it
		 * shows the not allowed page (the controller forbidden method).
		 * 
		 * @param profile
		 */
		static void onCheckFailed() {
			redirect(UserServiceFactory.getUserService().createLoginURL("/"));
		}
		
		 private static Object invoke(String m, Object... args) throws Throwable {
	            Class security = null;
	            List<Class> classes = Play.classloader.getAssignableClasses(Security.class);
	            if(classes.size() == 0) {
	                security = Security.class;
	            } else {
	                security = classes.get(0);
	            }
	            try {
	                return Java.invokeStaticOrParent(security, m, args);
	            } catch(InvocationTargetException e) {
	                throw e.getTargetException();
	            }
	        }
	}
}
