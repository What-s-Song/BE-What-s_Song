package dy.whatsong.domain.reservation.application.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dy.whatsong.domain.reservation.application.service.ReservationService;
import dy.whatsong.domain.reservation.dto.ReservationDTO;
import dy.whatsong.domain.reservation.entity.Recognize;
import dy.whatsong.domain.reservation.entity.Reservation;
import dy.whatsong.domain.reservation.repo.ReservationRepository;
import dy.whatsong.domain.streaming.application.service.RoomSseService;
import dy.whatsong.domain.youtube.dto.VideoDTO;
import dy.whatsong.global.annotation.EssentialServiceLayer;
import dy.whatsong.global.handler.exception.InvalidRequestAPIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EssentialServiceLayer
@RequiredArgsConstructor
@Log4j2
public class ReservationServiceImpl implements ReservationService {

	private final JPAQueryFactory jpaQueryFactory;

	private final ReservationRepository reservationRepository;

	private final RoomSseService roomSseService;

	@Override
	public ResponseEntity<?> reservationMusic(ReservationDTO.Select selectDTO) {
		Reservation currentReservation = reservationRepository.save(Reservation.builder()
				.reservationId(UUID.randomUUID().toString())
				.roomSeq(selectDTO.getRoomSeq())
				.selectVideo(ReservationDTO.SelectVideo
						.builder()
						.videoId(selectDTO.getVideoId())
						.channelName(selectDTO.getChannelName())
						.thumbnailUrl(selectDTO.getThumbnailUrl())
						.title(selectDTO.getTitle())
						.build())
				.recognize(Recognize.NONE)
				.build());

		return new ResponseEntity<>(currentReservation, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> reservationList(Long roomSeq) {
		System.out.println("roomSeq="+roomSeq);
		List<Reservation> reservationList=new ArrayList<>();

		reservationRepository.findAll()
				.forEach(reservation -> {
					if(Optional.ofNullable(reservation).isPresent()&&reservation.getRoomSeq().equals(roomSeq)){
						reservationList.add(reservation);
					}
				});
		System.out.println(reservationList.toString());
		return new ResponseEntity<>(reservationList,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> approveReservation(final ReservationDTO.Approve approveDTO) {
		Optional<Reservation> findOptionReservation = reservationRepository.findById(approveDTO.getReservationId());
		if (findOptionReservation.isEmpty()){
			throw new InvalidRequestAPIException("Invalid Request",400);
		}
		Reservation reSaveReserv = reSaveReservationEntity(
				findOptionReservation.get()
				, approveDTO.getRecognize());
		return new ResponseEntity<>(
					roomSseService.getCurrentReservationList(reSaveReserv)
					,HttpStatus.OK);
	}

	private Reservation reSaveReservationEntity(final Reservation reservation,final Recognize changeRecognize){
		Reservation changeReserEntity = Reservation.builder()
				.reservationId(reservation.getReservationId())
				.selectVideo(reservation.getSelectVideo())
				.recognize(changeRecognize)
				.roomSeq(reservation.getRoomSeq())
				.build();

		reservationRepository.delete(reservation);
		return reservationRepository.save(changeReserEntity);
	}
}
