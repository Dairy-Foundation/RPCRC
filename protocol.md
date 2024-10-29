# The RPCRC communication protocol

RPCRC aims to provide a simple to work with and light-weight RPC system suited to usage in FTC robots,
in order to enable external processes to control the robot in all signifigant aspects.

RPCRC communicates through a naively simplistic message format sent over network or unix file sockets.
This communication protocol is not suited to more real world applications where security is a concern.

RPCRC does not specify an encoding format for the whole system, instead, encoding is specified on a per-method basis.
Larger or more complex methods may decide to use json or protobuf formats, while smaller methods may do a direct binary encoding.

## General

Message contents are encoded as UTF-8 text unless otherwise specified.

RPCRC defines a limited number of configuration messages in the reserved path-spaces `/rpcrc/**` and `/rpc/**`,
as outlined [later in this document](#reserved-space-defined-methods).
RPCRC extensions that are not officially endorsed may not offer methods in these reserved path-spaces.

RPCRC does not define a client and a a server so much, instead the protocol serves to facilitate an equal two-way communication.
In the case of the robot and the remote process robot controller, the majority of the method calling is likely to be done by the remote process,
which might also be set up to only make one active connection at a time, thus making the relationship more server-client like.

## Requests

An RPCRC method call message takes the following format:

note: the `\n` newline indicators, this breakdown has newlines added for ease of reading, messages are encoded with only with the newline characters specified.

```
0
LENGTH
ENDPOINT_PATH
CALLBACK_ID
SERIALISED_MESSAGE_CONTENTS\n\n
```

## Responses

An RPCRC method call response message takes the following format:

note: the `\n` newline indicators, newlines have been added after them for ease of reading,
messages are encoded with only one newline at the end of a line.

```
1
STATUS_CODE
CALLBACK_ID
SERIALISED_MESSAGE_CONTENTS\n\n
```

## Definitions

### `LENGTH`

The length of `ENDPOINT_PATH` in bytes, encoded as 15 bit unsigned integer.
This means it has the hex range [`0x00`, `0x7F`].

This comes directly after the first 0 bit, indicating that this message is a request.

### `ENDPOINT_PATH`

The UTF-8 encoded path to the handler endpoint, using the `/` character as a separator, method paths are defined by implementations.

Paths should start with the root `/`.

e.g: `/path/to/method`

### `STATUS_CODE`

The status code of the operation, represented as a 7 bit unsigned integer. 
This means it has the hex range [`0x00`, `0x7F`].

This comes directly after the first 1 bit, indicating that this message is a response.

A returned value of `0x00` is used to indicate success, any other status code should be interpreted as a failure

// TODO: update these
A returned value of `0x7F` is used to indicate an otherwise unidentified internal server issue

A returned value of `0x7E` is used to indicate a malformed packet, this could be a missing header, malformed header, unterminated packet, etc.

A returned value of `0x7D` is used to indicate that the requested `ENDPOINT_PATH` is invalid, as there is no implementation for it on the other side

A returned value of `0x7C` is used to indicate that the supplied `SERIALISED_MESSAGE_CONTENTS` does not match any standard encoding.

A returned value of `0x7B` is used to indicate that the supplied `SERIALISED_MESSAGE_CONTENTS` does not match the expected encoding by the endpoint.

A returned value of `0x7A` is used to indicate that the `CALLBACK_ID` is invalid, as it is not in use, as it is already in use, and the defined method did not expect a message.

Methods that define no specific error status codes should return a value of `1` [`0x1`] to indicate a method-specific, but otherwise undefined failure.

Client implementations of RPCRC will return a status code of `1` [`0x1`] should a method return an error or throw an exception during its operation that was not handled by the method itself.

Methods have no obligation to include any data in the `data` field for an erroring response, but it is good practice to return information to help the other side of the connection diagnose, or appropriately handle the erroring response.

Erroring responses should always return UTF-8 encoded text as their `data` field contents.

For this reason, client implementations of RPCRC are required to return as helpful of a stack trace or similar error log as possible with the default `status` response of `1`.

Methods should avoid reserving a `STATUS_CODE` of `1` for method-specific responses, as it makes diagnosing issues difficult for the other end of the connection.

### `CALLBACK_ID`

The callback id number encoded as a 2 byte unsigned integer (big endian), a response will return the callback_id of the call its responding to.

It is important that in-flight callback ids do not overlap from the same method caller, and they are a resource solely managed by the method caller.

The callback id `0` generally indicates success, all other callback ids are usually interpreted as failures of some description
The callback id `65535 ` [`0xFFFF`] is reserved for broadcast errors, due to failing to parse the callback_id 

### `SERIALISED_MESSAGE_CONTENTS`

This supports many 'pre-built' encodings:


## Reserved-Space Defined Methods

Note: `!ENDPOINT` means that a section here does not refer to an endpoint.
This should be accompanied by a glob-star patten, to help clarify which sub-points the section describes.

`/rpc/**` methods are necessary for operation as the implementation will call them and thus MUST be included for the connection to work.
`/rpcrc/**` methods are not necessary for operation, but user code that interfaces with implementations should assume that these are implemented, unless otherwise declared.

### `/rpc/ping`
Pings the other end of the connection. It responds with the same bytes.


### `/rpcrc/query/**` `!ENDPOINT`
Queries for the capabilities other side of the connection.

Queryable endpoints all take an empty `data` header (0 length), and are used to discover more about the other end of the connection.

#### `/rpcrc/query/endpoints`
Returns utf-8 encoded, `\n` separated list of endpoints in the form of method paths.

#### `/rpcrc/query/queries`
Returns utf-8 encoded, `\n` separated list queryable endpoints in the form of method paths.
This list includes queryable endpoints in `/rpcrc/query/**`

### Pub-Sub

#### `/rpcrc/pub`

Updates the other end of the connection about these published values. This method will return `2` [`0x2`] if any published values are not subscribed to

#### `/rpcrc/pub/dropped`

Updates the other end of the connection that an end point that they are subscribed to has been removed, and will not longer be published.

`TODO`

#### `/rpcrc/pub/configure`

Configures publication behaviours of the other end of the connection

`TODO`

#### `/rpcrc/pub/update`

Manually requests an `/rpcrc/pub` update

`TODO`

#### `/rpcrc/sub`

Lets the other end of the connection know that this connection should receive updates (subscribe) or stop doing so (un-subscribe) about these published values.

`SERIALIZED_MESSAGE_CONTENTS` should be utf-8 encoded, `\n` separated list of endpoints in the form of method paths that this client wishes to subscribe to.
These support glob-star pattern matching (see below), in order to subscribe to multiple at once, or to pre-emptively subscribe to future published topics.

Each line must start with either `+` or `-`, to indicate either a subscription or un-subscription respectively.

Subscribing or un-subscribing from a value endpoint that is already in that state does not cause an issue.
Additionally, subscription and un-subscription is performed in the order given

The pub-sub endpoint paths are not shared with method endpoints.

This method returns a simple acknowledgement.

This method will return `2` [`0x2`] if any lines do not start with `+` or `-`

Glob-star Matching:

The final endpoint component of a subscribed endpoint may not be the reserved sequences `*` or `**`.
These are the all, and recursive-all patterns respectively.

The `*` pattern matches all endpoints at this point.
E.g.: `+/var/*` will subscribe to all endpoints directly after `/var`.
`/var/a` will be subscribed to, but `/var/a/b` will not be.

The `**` pattern matches all endpoints at this point.
E.g.: `+/var/**` will subscribe to all endpoints after `/var`.
`/var/a` will be subscribed to, as will `/var/a/b`.

E.g.: To subscribe to all endpoints the `+/**` path will match all end points.

#### `/rpcrc/sub/query/available`

Queries the other end of the connection for currently available published topics

#### `/rpcrc/sub/query/subscribed`

Queries the other end of the connection for currently available published topics that this connection is subscribed to
