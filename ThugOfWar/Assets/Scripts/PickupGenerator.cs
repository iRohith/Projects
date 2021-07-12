using System;
using System.Collections.Generic;
using UnityEngine;
using Mirror;

public class PickupGenerator : NetworkBehaviour
{
    [Serializable]
    public struct PickupProbability {
        [Range(0,1)]
        public float Probability;
        public GameObject PickupPrefab;
    }

    public int maxPickips = 100;
    public float pickupsPerBatch = 20f;
    public float interval = 1;
    public float radius = 5f;
    public Vector2 center, stdDev;
    public Transform followTransform;
    public List<PickupProbability> pickupPrefabs;

    private float nextUpdateTime = 0;
    internal List<GameObject> pickups = new List<GameObject>();

    // Update is called once per frame
    void Update()
    {
        if (!isServer) return;
        float now = Time.time;
        if (now > nextUpdateTime)
        {
            nextUpdateTime = now + interval;
            if (pickups.Count < maxPickips)
            {
                if (followTransform != null) center = new Vector2(followTransform.position.x, followTransform.position.y);
                Vector2 pos;
                for (int i=0; i < pickupsPerBatch; i++){
                    pos = radius * RandomGaussianCircle(Vector2.zero, stdDev) + center;
                    var p = Instantiate(SelectPickup(), new Vector3(pos.x,pos.y), Quaternion.Euler(Vector3.zero), transform);
                    p.GetComponent<ScoreCounter>().pickupGenerator = this;
                    NetworkServer.Spawn(p);
                    pickups.Add(p);
                }
            }
        }
    }

    GameObject SelectPickup(){
        float rand = UnityEngine.Random.value, tmp = 0;
        foreach (var pp in pickupPrefabs){
            tmp += pp.Probability;
            if (rand < tmp) return pp.PickupPrefab;
        }
        return pickupPrefabs[0].PickupPrefab;
    }

    public static Vector2 RandomGaussianCircle(Vector2 mean, Vector2 stdDev)
    {
        float u, v, s;
        do
        {
            u = UnityEngine.Random.value * 2 - 1;
            v = UnityEngine.Random.value * 2 - 1;
            s = u * u + v * v;
        } while (s >= 1 || s == 0);
        s = Mathf.Sqrt(-2.0f * Mathf.Log(s) / s);
        return new Vector2(mean.x + stdDev.x * u * s, mean.y + stdDev.y * v * s);
    }
    public static Vector2 RandomGuassianUnitCircle(){
        float u, v, s;
        do
        {
            u = UnityEngine.Random.value * 2 - 1;
            v = UnityEngine.Random.value * 2 - 1;
            s = u * u + v * v;
        } while (s >= 1 || s == 0);
        s = Mathf.Sqrt(-2.0f * Mathf.Log(s) / s);
        return new Vector2(u*s, v*s);
    }
}
