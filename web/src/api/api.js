import axios from "axios/index";

const instance = axios.create({
    timeout: 500
});

class DronApi {
    getDrones() {
        return instance.get('/drones').then(response => response.data);
    }

    createDrones(number) {
        return instance.post('/drones', {number: number});
    }
}

class OrderApi {
    createOrder(loc) {
        return instance.post('/orders', {loc: loc});
    }
}

export const dronApi = new DronApi();
export const orderApi = new OrderApi();