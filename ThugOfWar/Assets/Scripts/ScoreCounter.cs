using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Mirror;

public class ScoreCounter : NetworkBehaviour
{
    public PickupGenerator pickupGenerator;
    public int pickupTypeIndex;

    void OnTriggerEnter2D(Collider2D col)
    {
        LocalController controller;
        if (isServer && col.name == "dog0" && col.transform.parent.gameObject.TryGetComponent<LocalController>(out controller)){
            switch (pickupTypeIndex){
                case 1 : controller.chainNodes++; break;
                case 2 : if (controller.chainNodes > 2) controller.chainNodes--; break;
                default: controller.score++; break;
            }
            pickupGenerator.pickups.Remove(gameObject);
            NetworkServer.Destroy(gameObject);
        }
    }
}
