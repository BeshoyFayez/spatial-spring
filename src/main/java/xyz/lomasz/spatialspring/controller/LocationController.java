package xyz.lomasz.spatialspring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import xyz.lomasz.spatialspring.service.LocationService;

import java.util.Optional;

@RestController
public class LocationController {

    private static final String AUTHORIZATION = "Authorization";

    private LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public ResponseEntity postLocation(@RequestHeader(value = AUTHORIZATION) String userId,
                                       @RequestBody Feature feature) {

        Long id = locationService.saveLocation(userId, feature);

        UriComponentsBuilder ucBuilder = UriComponentsBuilder.newInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/location/{id}").buildAndExpand(id).toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/location/{id}", method = RequestMethod.GET)
    public ResponseEntity getLocationById(@RequestHeader(value = AUTHORIZATION) String userId,
                                          @PathVariable("id") Long id) {

        Optional<Feature> location = locationService.findLocationById(userId, id);
        return location.map(i -> new ResponseEntity<>(i, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/location/{id}", method = RequestMethod.PUT)
    public ResponseEntity putLocation(@RequestHeader(value = AUTHORIZATION) String userId,
                                      @PathVariable("id") Long id,
                                      @RequestBody Feature feature) {

        if (!locationService.exists(userId, id)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        locationService.updateLocation(userId, id, feature);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/location/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteLocation(@RequestHeader(value = AUTHORIZATION) String userId,
                                         @PathVariable("id") Long id) {

        if (!locationService.exists(userId, id)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        locationService.deleteLocation(userId, id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/locations", method = RequestMethod.GET)
    public ResponseEntity<FeatureCollection> getAllLocations(@RequestHeader(value = AUTHORIZATION) String userId) {
        return new ResponseEntity<>(locationService.findAllLocations(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/locations/within", method = RequestMethod.POST)
    public ResponseEntity<FeatureCollection> getLocationsByGeometry(@RequestHeader(value = AUTHORIZATION) String userId,
                                                                    @RequestBody org.wololo.geojson.Geometry geoJson) {

        return new ResponseEntity<>(locationService.findAllLocationsWithin(userId, geoJson), HttpStatus.OK);
    }
}
