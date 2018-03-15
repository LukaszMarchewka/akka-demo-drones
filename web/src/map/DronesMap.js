import React, {Component} from 'react';
import Map from "./Map";
import {dronApi, DronApi, orderApi} from "../api/api";

export default class DronesMap extends Component {

    constructor(props) {
        super(props);
        this.state = {
            drones: []
        };
        this.loadDrones();
    }

    render() {
        const markers = this.state.drones.map(drone => new Object({
            lat: drone.loc.lat,
            long: drone.loc.long,
            title: drone.id,
            opacity: drone.age < 10 ? (10 - drone.age) / 10 : 1,
            icon: this.getIcon(drone)
        }));
        return (
            <Map center={{lat: 53.117046, long: 23.146447}}
                 markers={markers}
                 onMapClick={loc => this.createOrder(loc)}/>
        );
    }

    loadDrones() {
        dronApi.getDrones().then(drones => this.setState({drones: drones}))
            .then(a => setTimeout(() => this.loadDrones(), 1000))
            .catch(a => setTimeout(() => this.loadDrones(), 1000));
    }

    createOrder(loc) {
        orderApi.createOrder(Date.now().toString(), loc);
    }

    getIcon(drone) {
        if (drone.age >= 10)
            return "/marker/death.png";
        else
            return "/marker/red.png";
    }
}