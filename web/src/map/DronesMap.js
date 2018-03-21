import React, {Component} from 'react';
import Map from "./Map";
import {dronApi, DronApi, orderApi} from "../api/api";

export default class DronesMap extends Component {

    constructor(props) {
        super(props);
        this.state = {
            drones: [],
            orders: []
        };
        this.loadDrones();
        this.loadOrders();
    }

    render() {
        const dronesMarkers = this.state.drones.map(drone => new Object({
            lat: drone.current.lat,
            long: drone.current.long,
            title: drone.id,
            opacity: drone.age < 10 ? (10 - drone.age) / 10 : 1,
            icon: this.getDroneIcon(drone)
        }));
        const ordersMarkers = this.state.orders.map(order => new Object({
            lat: order.loc.lat,
            long: order.loc.long,
            title: order.id,
            icon: this.getOrderIcon(order)
        }));
        return (
            <Map center={{lat: 53.117046, long: 23.146447}}
                 markers={dronesMarkers.concat(ordersMarkers)}
                 onMapClick={loc => this.createOrder(loc)}/>
        );
    }

    loadDrones() {
        dronApi.getDrones().then(drones => this.setState({drones: drones}))
            .then(a => setTimeout(() => this.loadDrones(), 1000))
            .catch(a => setTimeout(() => this.loadDrones(), 1000));
    }

    loadOrders() {
        orderApi.getOrders().then(orders => this.setState({orders: orders}))
            .then(a => setTimeout(() => this.loadOrders(), 1000))
            .catch(a => setTimeout(() => this.loadOrders(), 1000));
    }

    createOrder(loc) {
        orderApi.createOrder(loc);
    }

    getDroneIcon(drone) {
        if (drone.age >= 10)
            return "/marker/death.png";
        else if (drone.orderId)
            return "/marker/fly_red.png";
        else if (drone.target)
            return "/marker/fly_green.png";
        else
            return "/marker/blue.png";
    }

    getOrderIcon(order) {
        if (order.droneId !== undefined)
            return "/marker/person_red.png";
        else
            return "/marker/person_green.png";
    }
}