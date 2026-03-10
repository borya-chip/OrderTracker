package com.order.tracker.service.impl;

import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.dto.response.RestaurantResponse;
import com.order.tracker.mapper.RestaurantMapper;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    @Override
    @Transactional
    public RestaurantResponse create(final RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        apply(restaurant, request);
        return restaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse getById(final Long id) {
        return restaurantMapper.toResponse(findRestaurant(id));
    }

    @Override
    public List<RestaurantResponse> getAll() {
        return restaurantRepository.findAll().stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RestaurantResponse update(final Long id, final RestaurantRequest request) {
        Restaurant restaurant = findRestaurant(id);
        apply(restaurant, request);
        return restaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: " + id);
        }
        restaurantRepository.deleteById(id);
    }

    private Restaurant findRestaurant(final Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: " + id));
    }

    private void apply(final Restaurant restaurant, final RestaurantRequest request) {
        restaurant.setName(request.getName());
        restaurant.setContactEmail(request.getContactEmail());
        restaurant.setCity(request.getCity());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
    }
}
