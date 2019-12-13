
package acme.features.authenticated.thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.threads.Thread;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.entities.Authenticated;
import acme.framework.services.AbstractShowService;

@Service
public class AuthenticatedThreadShowService implements AbstractShowService<Authenticated, Thread> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticatedThreadRepository repository;


	@Override
	public boolean authorise(final Request<Thread> request) {
		assert request != null;

		Boolean result;
		int threadId;
		Thread thread;

		threadId = request.getModel().getInteger("id");
		thread = this.repository.findOneThreadById(threadId);
		result = thread.getUsers().stream().map(u -> u.getUserAccount().getId()).anyMatch(i -> request.getPrincipal().getAccountId() == i);

		return result;
	}

	@Override
	public void unbind(final Request<Thread> request, final Thread entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		String direccion = "../message/list_by_thread?id=" + entity.getId();
		model.setAttribute("direccion", direccion);
		request.unbind(entity, model, "title", "creationDate");

		String direccion2 = "../message/create?id=" + entity.getId();
		model.setAttribute("threadCreateMessage", direccion2);

		int idThread = request.getModel().getInteger("id");
		String direccionThreadUpdate = "../thread/update?id=" + idThread;
		model.setAttribute("direccionThreadUpdate", direccionThreadUpdate);

		int idThread2 = entity.getId();
		String direccionAnadirUsuario = "../authenticated/list?threadid=" + idThread2;
		model.setAttribute("direccionAnadirUsuario", direccionAnadirUsuario);
	}

	@Override
	public Thread findOne(final Request<Thread> request) {
		assert request != null;

		Thread result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findOneThreadById(id);
		//		result.getMessages().size();

		return result;
	}
}
