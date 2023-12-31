package tn.esprit.spring.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.esprit.spring.entities.HistoriqueEntity;
import tn.esprit.spring.entities.PlaceParking;
import tn.esprit.spring.entities.UserEntity;
import tn.esprit.spring.repositories.ParkingRepository;
import tn.esprit.spring.repositories.PlaceParkingRepository;
import tn.esprit.spring.repositories.UserRepository;
import tn.esprit.spring.repositories.historyRepository;
import tn.esprit.spring.requests.PlaceParkingBooking;
import tn.esprit.spring.services.EmailService;
import tn.esprit.spring.services.PlaceParkingService;

@Service
//Impl�mentation de l'interface PlaceParkingService d�finissant les op�rations de gestion des places de parking.
public class PlaceParkingServiceImpl implements PlaceParkingService {

	// Injection de d�pendances des repositories et services n�cessaires.
	@Autowired
	PlaceParkingRepository placeparkingRepository;

	@Autowired
	ParkingRepository parkingRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	historyRepository historyRepository;

	@Autowired
	private EmailService emailService;

	// M�thode pour r�cup�rer toutes les places de parking disponibles.
	@Override
	public List<PlaceParking> retrieveAllPlaceParkings() {
		return (List<PlaceParking>) placeparkingRepository.findAll();
	}

	// M�thode pour ajouter une nouvelle place de parking � un parking sp�cifi�.
	@Override
	public PlaceParking addPlaceParking(long id, PlaceParking pp) {
		parkingRepository.findById(id);
		pp.setParking(parkingRepository.findById(id).get());
		placeparkingRepository.save(pp);
		return pp;
	}

	// M�thode pour supprimer une place de parking en fonction de son ID.
	@Override
	public void deletePlaceParking(Long id) {
		PlaceParking place = placeparkingRepository.findById(id).get();
		place.setUser(null);
		placeparkingRepository.deleteById(id);
	}

	// M�thode pour r�cup�rer les informations d'une place de parking en fonction de
	// son ID.
	@Override
	public Optional<PlaceParking> retrievePlaceParking(Long id) {
		return placeparkingRepository.findById(id);
	}

	// M�thode pour mettre � jour les informations d'une place de parking.
	@Override
	public PlaceParking updatePlaceParking(long id, PlaceParking placeparking) {
		parkingRepository.findById(id);
		placeparking.setParking(parkingRepository.findById(id).get());
		return placeparkingRepository.save(placeparking);
	}

	// M�thode pour r�server une place de parking pour un utilisateur.
	@Override
	public PlaceParking Book(Long placeId, String userEmail, PlaceParkingBooking dates) {
		LocalDateTime now = LocalDateTime.now();
		UserEntity user = userRepository.findByEmail(userEmail);
		PlaceParking place = placeparkingRepository.findById(placeId).get();
		place.setUser(user);
		place.setStatus(false);
		place.setReservation(now);
		place.setStartDate(dates.getStartDate());
		place.setEndDate(dates.getEndDate());
		return placeparkingRepository.save(place);
	}

	// M�thode pour effectuer une r�servation de place de parking.
	@Override
	public PlaceParking Reserver(Long placeId, PlaceParkingBooking dates) {
		LocalDateTime now = LocalDateTime.now();
		PlaceParking place = placeparkingRepository.findById(placeId).get();
		place.setReservation(now);
		place.setStatus(dates.getStatus());
		place.setStartDate(dates.getStartDate());
		place.setEndDate(dates.getEndDate());
		PlaceParking place2 = placeparkingRepository.save(place);
		emailService.sendEmailWithTemplate(place.getUser());

		HistoriqueEntity histoire = new HistoriqueEntity();
		histoire.setEndDate(place.getEndDate());
		histoire.setUser(place.getUser().getUserId());
		histoire.setStartingDate(place.getStartDate());
		historyRepository.save(histoire);

		return place2;
	}

	// M�thode pour annuler une r�servation de place de parking.
	@Override
	public void cancelReserver(Long placeId) {
		PlaceParking place = placeparkingRepository.findById(placeId).get();
		place.setUser(null);
		place.setStatus(false);
		placeparkingRepository.save(place);
	}

	// M�thode pour r�cup�rer la liste des places de parking r�serv�es.
	@Override
	public List<PlaceParking> getBookedPlaces() {
		List<PlaceParking> places = new ArrayList<>();
		List<PlaceParking> placeParking = placeparkingRepository.findAll();
		for (int i = 0; i < placeParking.size(); i++) {
			if (placeParking.get(i).getStatus() == false) {
				UserEntity user = placeParking.get(i).getUser();
				if (user != null) {
					places.add(placeParking.get(i));
				}
			}
		}
		return places;
	}

	// M�thode pour r�cup�rer la liste des places de parking r�serv�es.
	@Override
	public List<PlaceParking> getReservedPlaces() {
		List<PlaceParking> places = new ArrayList<>();
		List<PlaceParking> placeParking = placeparkingRepository.findAll();
		for (int i = 0; i < placeParking.size(); i++) {
			if (placeParking.get(i).getStatus() == true) {
				UserEntity user = placeParking.get(i).getUser();
				if (user != null) {
					places.add(placeParking.get(i));
				}
			}
		}
		return places;
	}

	// M�thode pour r�server une place de parking pour une personne sp�cifique.
	@Override
	public PlaceParking ReserverPersonne(Long placeId, PlaceParkingBooking dates, String userId) {
		LocalDateTime now = LocalDateTime.now();
		PlaceParking place = placeparkingRepository.findById(placeId).get();
		place.setReservation(now);
		UserEntity user = userRepository.findByUserId(userId);
		place.setUser(user);
		place.setStatus(dates.getStatus());
		place.setStartDate(dates.getStartDate());
		place.setEndDate(dates.getEndDate());
		return placeparkingRepository.save(place);
	}
}
