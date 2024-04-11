package auctions;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: Auctions.proto")
public final class KademliaGrpc {

  private KademliaGrpc() {}

  public static final String SERVICE_NAME = "auctions.Kademlia";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<auctions.PingRequest,
      auctions.PingResponse> METHOD_PING =
      io.grpc.MethodDescriptor.<auctions.PingRequest, auctions.PingResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "ping"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.PingRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.PingResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<auctions.StoreRequest,
      auctions.StoreResponse> METHOD_STORE =
      io.grpc.MethodDescriptor.<auctions.StoreRequest, auctions.StoreResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "store"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.StoreRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.StoreResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<auctions.FindNodeRequest,
      auctions.FindNodeResponse> METHOD_FIND_NODE =
      io.grpc.MethodDescriptor.<auctions.FindNodeRequest, auctions.FindNodeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "findNode"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.FindNodeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.FindNodeResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<auctions.FindValueRequest,
      auctions.FindValueResponse> METHOD_FIND_VALUE =
      io.grpc.MethodDescriptor.<auctions.FindValueRequest, auctions.FindValueResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "findValue"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.FindValueRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.FindValueResponse.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KademliaStub newStub(io.grpc.Channel channel) {
    return new KademliaStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KademliaBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new KademliaBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KademliaFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new KademliaFutureStub(channel);
  }

  /**
   */
  public static abstract class KademliaImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Unary
     * </pre>
     */
    public void ping(auctions.PingRequest request,
        io.grpc.stub.StreamObserver<auctions.PingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PING, responseObserver);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public void store(auctions.StoreRequest request,
        io.grpc.stub.StreamObserver<auctions.StoreResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STORE, responseObserver);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(auctions.FindNodeRequest request,
        io.grpc.stub.StreamObserver<auctions.FindNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_NODE, responseObserver);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public void findValue(auctions.FindValueRequest request,
        io.grpc.stub.StreamObserver<auctions.FindValueResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_VALUE, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_PING,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.PingRequest,
                auctions.PingResponse>(
                  this, METHODID_PING)))
          .addMethod(
            METHOD_STORE,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.StoreRequest,
                auctions.StoreResponse>(
                  this, METHODID_STORE)))
          .addMethod(
            METHOD_FIND_NODE,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.FindNodeRequest,
                auctions.FindNodeResponse>(
                  this, METHODID_FIND_NODE)))
          .addMethod(
            METHOD_FIND_VALUE,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.FindValueRequest,
                auctions.FindValueResponse>(
                  this, METHODID_FIND_VALUE)))
          .build();
    }
  }

  /**
   */
  public static final class KademliaStub extends io.grpc.stub.AbstractStub<KademliaStub> {
    private KademliaStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unary
     * </pre>
     */
    public void ping(auctions.PingRequest request,
        io.grpc.stub.StreamObserver<auctions.PingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public void store(auctions.StoreRequest request,
        io.grpc.stub.StreamObserver<auctions.StoreResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STORE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(auctions.FindNodeRequest request,
        io.grpc.stub.StreamObserver<auctions.FindNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public void findValue(auctions.FindValueRequest request,
        io.grpc.stub.StreamObserver<auctions.FindValueResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_VALUE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class KademliaBlockingStub extends io.grpc.stub.AbstractStub<KademliaBlockingStub> {
    private KademliaBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unary
     * </pre>
     */
    public auctions.PingResponse ping(auctions.PingRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PING, getCallOptions(), request);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public auctions.StoreResponse store(auctions.StoreRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STORE, getCallOptions(), request);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public auctions.FindNodeResponse findNode(auctions.FindNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_NODE, getCallOptions(), request);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public auctions.FindValueResponse findValue(auctions.FindValueRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_VALUE, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class KademliaFutureStub extends io.grpc.stub.AbstractStub<KademliaFutureStub> {
    private KademliaFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KademliaFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KademliaFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KademliaFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Unary
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<auctions.PingResponse> ping(
        auctions.PingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<auctions.StoreResponse> store(
        auctions.StoreRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STORE, getCallOptions()), request);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<auctions.FindNodeResponse> findNode(
        auctions.FindNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<auctions.FindValueResponse> findValue(
        auctions.FindValueRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_VALUE, getCallOptions()), request);
    }
  }

  private static final int METHODID_PING = 0;
  private static final int METHODID_STORE = 1;
  private static final int METHODID_FIND_NODE = 2;
  private static final int METHODID_FIND_VALUE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final KademliaImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(KademliaImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PING:
          serviceImpl.ping((auctions.PingRequest) request,
              (io.grpc.stub.StreamObserver<auctions.PingResponse>) responseObserver);
          break;
        case METHODID_STORE:
          serviceImpl.store((auctions.StoreRequest) request,
              (io.grpc.stub.StreamObserver<auctions.StoreResponse>) responseObserver);
          break;
        case METHODID_FIND_NODE:
          serviceImpl.findNode((auctions.FindNodeRequest) request,
              (io.grpc.stub.StreamObserver<auctions.FindNodeResponse>) responseObserver);
          break;
        case METHODID_FIND_VALUE:
          serviceImpl.findValue((auctions.FindValueRequest) request,
              (io.grpc.stub.StreamObserver<auctions.FindValueResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class KademliaDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return auctions.Auctions.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (KademliaGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KademliaDescriptorSupplier())
              .addMethod(METHOD_PING)
              .addMethod(METHOD_STORE)
              .addMethod(METHOD_FIND_NODE)
              .addMethod(METHOD_FIND_VALUE)
              .build();
        }
      }
    }
    return result;
  }
}
