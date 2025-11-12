package com.example.k5_iot_springboot.service;

import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.dto.reservation.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    ResponseDto<List<ReservationResponseDto>> getReservationsByTruck(Long truckId);

    ResponseDto<ReservationResponseDto> getReservation(Long truckId, Long reservationId);
}
