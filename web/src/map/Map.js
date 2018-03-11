import React from 'react';
import {compose, withProps} from "recompose";
import {GoogleMap, Marker, withGoogleMap, withScriptjs} from "react-google-maps";

const mapsKey = null; //todo configure key

const Map = compose(
    withProps({
        googleMapURL: `https://maps.googleapis.com/maps/api/js?key=${mapsKey}&v=3.exp&libraries=geometry,drawing,places`,
        loadingElement: <div style={{height: `100%`}}/>,
        containerElement: <div style={{height: `800px`}}/>,
        mapElement: <div style={{height: `100%`}}/>
    }),
    withScriptjs,
    withGoogleMap
)(props => {
    return (
        <GoogleMap
            defaultZoom={12}
            defaultCenter={{lat: props.center.lat, lng: props.center.long}}
            onClick={event => props.onMapClick({lat: event.latLng.lat(), long: event.latLng.lng()})}>
            {props.markers.map((marker, index) =>
                <Marker key={index}
                        position={{lat: marker.lat, lng: marker.long}}
                        title={marker.title}/>
            )}
        </GoogleMap>
    );
});
export default Map;