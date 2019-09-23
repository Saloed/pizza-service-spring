package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import kspt.pizzaservicespring.utils.MyResult
import kspt.pizzaservicespring.models.ClientWithPermission
import kspt.pizzaservicespring.models.fullPermission

object UserLogic {
    fun createClient(login: String, password: String): User? {
        val client = Client(login, password, "", "")
        return Client.repository.save(client)
    }

    fun create(currentUser: User?, login: String, password: String, role: UserRoleType): User? {
        if (currentUser !is Manager) return null
        return when (role) {
            UserRoleType.Client -> createClient(login, password)
            UserRoleType.Manager -> {

                val manager = Manager(login, password, "")
                Manager.repository.save(manager)
            }
            UserRoleType.Operator -> {
                val operator = Operator(login, password, -1)
                Operator.repository.save(operator)
            }
            UserRoleType.Courier -> {
                val courier = Courier(login, password)
                Courier.repository.save(courier)
            }
        }
    }

    fun listClients(user: User): MyResult<List<ClientWithPermission>> {
        if (user !is Manager) return MyResult.Error("No access")
        val result = Client.repository.findAll().map { it.fullPermission() }
        return MyResult.Success(result)
    }

}
