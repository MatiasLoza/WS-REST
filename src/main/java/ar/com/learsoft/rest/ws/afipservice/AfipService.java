package ar.com.learsoft.rest.ws.afipservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.validation.Validator;
import javax.xml.namespace.QName;

import ar.com.learsoft.rest.ws.connection.ServiceStatusDataBaseDAOImpl;
import ar.com.learsoft.rest.ws.model.Client;
import ar.com.learsoft.rest.ws.model.GracefulInputResponse;
import ar.com.learsoft.rest.ws.model.ServiceResponse;
import ar.com.learsoft.rest.ws.model.ServiceStatus;
import ar.com.learsoft.soap.ws.ServiceChecker;

@Service
public class AfipService {

	@Autowired
	private Validator validator;

	@Autowired
	private ServiceStatusDataBaseDAOImpl serviceStatusDataBaseDAOImpl;

	private ServiceChecker getWsSoapProxy() {
		URL url = null;
		try {
			url = new URL("http://localhost:8080/ws/afipchecker?wsdl");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		QName serviceName = new QName("http://ws.soap.learsoft.com.ar/", "AfipServiceCheckerService");
		QName portName = new QName("http://ws.soap.learsoft.com.ar/", "AfipServiceCheckerPort");
		javax.xml.ws.Service service = javax.xml.ws.Service.create(url, serviceName);
		return service.getPort(portName, ServiceChecker.class);
	}

	private GracefulInputResponse getResponseFromWsSoap() {
		ServiceChecker wsSoapProxy = getWsSoapProxy();
		String afipServiceStatus = wsSoapProxy.getStatus();
		return new GracefulInputResponse(afipServiceStatus);
	}

	public GracefulInputResponse getResponseFromSoapAndStoreItInDataBase(Client client) {
		GracefulInputResponse gracefulInputResponse = this.getResponseFromWsSoap();
		serviceStatusDataBaseDAOImpl.saveInDataBase(client, gracefulInputResponse);
		return gracefulInputResponse;
	}

	private void validateClient(Client client) {
		Set<ConstraintViolation<Client>> violations = validator.validate(client);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);

		}
	}

	public ServiceResponse check(Client client) {
		this.validateClient(client);
		return getResponseFromSoapAndStoreItInDataBase(client);
	}

	public List<ServiceStatus> searchByApplicationId(Client client) {
		this.validateClient(client);
		return serviceStatusDataBaseDAOImpl.searchByApplicationId(client);
	}

	public List<ServiceStatus> findByDate(Long firstDate, Long secondDate) {
		return serviceStatusDataBaseDAOImpl.findByDate(firstDate, secondDate);

	}

}