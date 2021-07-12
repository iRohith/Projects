using UnityEngine;

namespace Mirror.Experimental
{
    [AddComponentMenu("Network/Experimental/NetworkLerpRigidbody2D")]
    [HelpURL("https://mirror-networking.com/docs/Components/NetworkLerpRigidbody.html")]
    public class NetworkLerpRigidbody2D : NetworkBehaviour
    {
        [Header("Settings")]
        [SerializeField] internal Rigidbody2D target = null;
        [Tooltip("How quickly current velocity approaches target velocity")]
        [SerializeField] float lerpVelocityAmount = 0.5f;
        [Tooltip("How quickly current position approaches target position")]
        [SerializeField] float lerpPositionAmount = 0.5f;
        [Tooltip("How quickly current angular velocity approaches target position")]
        [SerializeField] float lerpAngularVelocityAmount = 0.5f;
        [Tooltip("How quickly current position approaches target position")]
        [SerializeField] float lerpRotationAmount = 0.5f;

        [Tooltip("Set to true if moves come from owner client, set to false if moves always come from server")]
        [SerializeField] bool clientAuthority = false;

        float nextSyncTime;


        [SyncVar()]
        Vector2 targetVelocity;

        [SyncVar()]
        Vector2 targetPosition;

        [SyncVar()]
        float targetAngularVelocity;

        [SyncVar()]
        float targetRotation;

        [SyncVar()]
        RigidbodyType2D targetBodyType;

        /// <summary>
        /// Ignore value if is host or client with Authority
        /// </summary>
        /// <returns></returns>
        bool IgnoreSync => isServer || ClientWithAuthority;

        bool ClientWithAuthority => clientAuthority && hasAuthority;

        void OnValidate()
        {
            if (target == null)
            {
                target = GetComponent<Rigidbody2D>();
            }
        }

        void Update()
        {
            if (isServer)
            {
                SyncToClients();
            }
            else if (ClientWithAuthority)
            {
                SendToServer();
            }
        }

        private void SyncToClients()
        {
            targetVelocity = target.velocity;
            targetPosition = target.position;
            targetAngularVelocity = target.angularVelocity;
            targetRotation = target.rotation;
            targetBodyType = target.bodyType;
        }

        private void SendToServer()
        {
            float now = Time.time;
            if (now > nextSyncTime)
            {
                nextSyncTime = now + syncInterval;
                CmdSendState(target.velocity, target.position, target.angularVelocity, target.rotation, targetBodyType);
            }
        }

        [Command]
        private void CmdSendState(Vector2 velocity, Vector2 position, float angularVelocity, float rotation, RigidbodyType2D type)
        {
            target.velocity = velocity;
            target.position = position;
            target.angularVelocity = angularVelocity;
            target.rotation = rotation;
            target.bodyType = type;
            targetBodyType = type;
            targetVelocity = velocity;
            targetPosition = position;
            targetAngularVelocity = angularVelocity;
            targetRotation = rotation;

        }

        void FixedUpdate()
        {
            if (IgnoreSync) { return; }

            target.velocity = Vector2.Lerp(target.velocity, targetVelocity, lerpVelocityAmount);
            target.position = Vector2.Lerp(target.position, targetPosition, lerpPositionAmount);
            target.angularVelocity = Mathf.Lerp(target.angularVelocity, targetAngularVelocity, lerpAngularVelocityAmount);
            target.rotation = Mathf.Lerp(target.rotation, targetRotation, lerpRotationAmount);
            // add velocity to position as position would have moved on server at that velocity
            targetPosition += target.velocity * Time.fixedDeltaTime;
            targetRotation += target.angularVelocity * Time.fixedDeltaTime;

            if (target.bodyType != targetBodyType) target.bodyType = targetBodyType;

            // TODO does this also need to sync acceleration so and update velocity?
        }
    }
}
