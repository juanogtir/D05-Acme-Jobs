
package acme.features.sponsor.nonCommercialBanner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.customization.Customization;
import acme.entities.nonCommercialBanners.NonCommercialBanner;
import acme.entities.roles.Sponsor;
import acme.framework.components.Errors;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.entities.Principal;
import acme.framework.services.AbstractUpdateService;

@Service
public class SponsorNonCommercialBannerUpdateService implements AbstractUpdateService<Sponsor, NonCommercialBanner> {

	@Autowired
	SponsorNonCommercialBannerRepository repository;


	@Override
	public boolean authorise(final Request<NonCommercialBanner> request) {
		assert request != null;

		boolean result;
		int nonCommercialBannerId;
		NonCommercialBanner nonCommercialBanner;
		Sponsor sponsor;
		Principal principal;

		nonCommercialBannerId = request.getModel().getInteger("id");
		nonCommercialBanner = this.repository.findOneNonCommercialBannerById(nonCommercialBannerId);
		sponsor = nonCommercialBanner.getSponsor();
		principal = request.getPrincipal();
		result = sponsor.getUserAccount().getId() == principal.getAccountId();

		return result;
	}

	@Override
	public void bind(final Request<NonCommercialBanner> request, final NonCommercialBanner entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;
		request.bind(entity, errors);
	}

	@Override
	public void unbind(final Request<NonCommercialBanner> request, final NonCommercialBanner entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;
		request.unbind(entity, model, "picture", "slogan", "url", "jingle");
	}

	@Override
	public NonCommercialBanner findOne(final Request<NonCommercialBanner> request) {
		assert request != null;

		NonCommercialBanner result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findOneNonCommercialBannerById(id);

		return result;
	}

	@Override
	public void validate(final Request<NonCommercialBanner> request, final NonCommercialBanner entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		String slogan = entity.getSlogan();
		String[] sloganArray = slogan.split(" ");

		Customization customisation = this.repository.findCustomization();

		String spamWords = customisation.getSpam();
		String[] spamArray = spamWords.split(",");
		Double threshold = customisation.getThreshold();

		List<String> spamList = IntStream.range(0, spamArray.length).boxed().map(x -> spamArray[x].trim()).collect(Collectors.toList());

		Integer numSpamSlogan = (int) IntStream.range(0, sloganArray.length).boxed().map(x -> sloganArray[x].trim()).filter(i -> spamList.contains(i)).count();

		boolean isFreeOfSpamSlogan = true;

		if (numSpamSlogan != 0) {
			isFreeOfSpamSlogan = 100 * numSpamSlogan / sloganArray.length < threshold;
		}

		errors.state(request, isFreeOfSpamSlogan, "slogan", "sponsor.NonCommercialBanner.spamWords");

	}

	@Override
	public void update(final Request<NonCommercialBanner> request, final NonCommercialBanner entity) {
		assert request != null;

		Sponsor sponsor;
		Principal principal;
		int userAccountId;

		// primero vamos a encontrar el Sponsor y vamos  agregarlo a dicho CommercialBanner
		principal = request.getPrincipal();
		userAccountId = principal.getAccountId();
		sponsor = this.repository.findOneSponsorByUserAccountId(userAccountId);

		entity.setSponsor(sponsor);

		this.repository.save(entity);

	}

}
