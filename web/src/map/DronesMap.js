import React, {Component} from 'react';
import Map from "./Map";
import {dronApi, DronApi, orderApi} from "../api/api";

export default class DronesMap extends Component {

    constructor(props) {
        super(props);
        this.state = {
            drones: []
        };
        setInterval(() => this.loadDrones(), 1000);
    }

    render() {
        const markers = this.state.drones.map(drone => new Object({
            lat: drone.loc.lat,
            long: drone.loc.long,
            title: drone.id
        }));
        return (
            <Map center={{lat: 53.117046, long: 23.146447}}
                 markers={markers}
                 onMapClick={loc => this.createOrder(loc)}/>
        );
    }

    loadDrones() {
        dronApi.getDrones().then(drones => this.setState({drones: drones}));
    }

    createOrder(loc) {
        orderApi.createOrder(Date.now().toString(), loc);
    }
}