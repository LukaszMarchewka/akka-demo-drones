import axios from "axios/index";

const instance = axios.create({
    timeout: 500
});

class DronApi {
    getDrones() {
        return instance.get('/drones').then(response => response.data);
    }
}

class OrderApi {
    getOrders() {
        return instance.get('/orders').then(response => response.data);
    }

    createOrder(loc) {
        return instance.post('/orders', {loc: loc});
    }
}

export const dronApi = new DronApi();
export const orderApi = new OrderApi();