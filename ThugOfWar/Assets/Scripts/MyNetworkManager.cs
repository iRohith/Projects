using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Mirror;

[AddComponentMenu("")]
public class MyNetworkManager : NetworkManager
{
    public Vector3 spawnPosition;
    public PickupGenerator pickupGenerator;

    public override void OnServerAddPlayer(NetworkConnection conn)
    {
        pickupGenerator.enabled = numPlayers + 1 == maxConnections;
        var rotation = Quaternion.Euler(0, 0, (numPlayers % 2 == 0 ? 0 : 180) + (numPlayers > 1 ? 90 : 0));
        GameObject player = Instantiate(playerPrefab, spawnPosition, rotation);
        player.name = "Player" + (numPlayers + 1);
        NetworkServer.AddPlayerForConnection(conn, player);

        if (numPlayers == maxConnections)
        {
            var ring = GameObject.Find("Ring");
            if (ring != null) ring.GetComponent<Rigidbody2D>().bodyType = RigidbodyType2D.Dynamic;
        }
    }

    public override void OnServerDisconnect(NetworkConnection conn)
    {
        if (numPlayers <= 2)
        {
            foreach (var pickup in pickupGenerator.pickups)
                NetworkServer.Destroy(pickup);
            var ring = GameObject.Find("Ring");
            var rb = ring.GetComponent<Rigidbody2D>();
            rb.velocity = Vector2.zero;
            rb.angularVelocity = 0;
            ring.transform.SetPositionAndRotation(Vector3.zero, Quaternion.Euler(Vector3.zero));
            rb.bodyType = RigidbodyType2D.Static;
            base.OnServerDisconnect(conn);
            NetworkServer.Shutdown();
        }
    }
}
