package kademlia;

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
    comments = "Source: Kademlia.proto")
public final class KademliaGrpc {

  private KademliaGrpc() {}

  public static final String SERVICE_NAME = "kademlia.Kademlia";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<kademlia.PingRequest,
      kademlia.PingResponse> METHOD_PING =
      io.grpc.MethodDescriptor.<kademlia.PingRequest, kademlia.PingResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "kademlia.Kademlia", "ping"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.PingRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.PingResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<kademlia.StoreRequest,
      kademlia.StoreResponse> METHOD_STORE =
      io.grpc.MethodDescriptor.<kademlia.StoreRequest, kademlia.StoreResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "kademlia.Kademlia", "store"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.StoreRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.StoreResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<kademlia.FindNodeRequest,
      kademlia.FindNodeResponse> METHOD_FIND_NODE =
      io.grpc.MethodDescriptor.<kademlia.FindNodeRequest, kademlia.FindNodeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "kademlia.Kademlia", "findNode"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindNodeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindNodeResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<kademlia.FindValueRequest,
      kademlia.FindValueResponse> METHOD_FIND_VALUE =
      io.grpc.MethodDescriptor.<kademlia.FindValueRequest, kademlia.FindValueResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "kademlia.Kademlia", "findValue"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindValueRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindValueResponse.getDefaultInstance()))
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
     *TODO Adicionar metodos pro Bootstrap node - Cristina
     *rpc iniateBootstrap()
     * Unary
     * </pre>
     */
    public void ping(kademlia.PingRequest request,
        io.grpc.stub.StreamObserver<kademlia.PingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PING, responseObserver);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public void store(kademlia.StoreRequest request,
        io.grpc.stub.StreamObserver<kademlia.StoreResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STORE, responseObserver);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_NODE, responseObserver);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public void findValue(kademlia.FindValueRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindValueResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_VALUE, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_PING,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.PingRequest,
                kademlia.PingResponse>(
                  this, METHODID_PING)))
          .addMethod(
            METHOD_STORE,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.StoreRequest,
                kademlia.StoreResponse>(
                  this, METHODID_STORE)))
          .addMethod(
            METHOD_FIND_NODE,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.FindNodeRequest,
                kademlia.FindNodeResponse>(
                  this, METHODID_FIND_NODE)))
          .addMethod(
            METHOD_FIND_VALUE,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.FindValueRequest,
                kademlia.FindValueResponse>(
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
     *TODO Adicionar metodos pro Bootstrap node - Cristina
     *rpc iniateBootstrap()
     * Unary
     * </pre>
     */
    public void ping(kademlia.PingRequest request,
        io.grpc.stub.StreamObserver<kademlia.PingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public void store(kademlia.StoreRequest request,
        io.grpc.stub.StreamObserver<kademlia.StoreResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STORE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public void findValue(kademlia.FindValueRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindValueResponse> responseObserver) {
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
     *TODO Adicionar metodos pro Bootstrap node - Cristina
     *rpc iniateBootstrap()
     * Unary
     * </pre>
     */
    public kademlia.PingResponse ping(kademlia.PingRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PING, getCallOptions(), request);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public kademlia.StoreResponse store(kademlia.StoreRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STORE, getCallOptions(), request);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public kademlia.FindNodeResponse findNode(kademlia.FindNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_NODE, getCallOptions(), request);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public kademlia.FindValueResponse findValue(kademlia.FindValueRequest request) {
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
     *TODO Adicionar metodos pro Bootstrap node - Cristina
     *rpc iniateBootstrap()
     * Unary
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.PingResponse> ping(
        kademlia.PingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request);
    }

    /**
     * <pre>
     * store a [key, value] pair for later retrieval
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.StoreResponse> store(
        kademlia.StoreRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STORE, getCallOptions()), request);
    }

    /**
     * <pre>
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.FindNodeResponse> findNode(
        kademlia.FindNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request);
    }

    /**
     * <pre>
     * similar to findNode + if RPC recipient received a STORE for the given key then it returns the stored value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.FindValueResponse> findValue(
        kademlia.FindValueRequest request) {
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
          serviceImpl.ping((kademlia.PingRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.PingResponse>) responseObserver);
          break;
        case METHODID_STORE:
          serviceImpl.store((kademlia.StoreRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.StoreResponse>) responseObserver);
          break;
        case METHODID_FIND_NODE:
          serviceImpl.findNode((kademlia.FindNodeRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.FindNodeResponse>) responseObserver);
          break;
        case METHODID_FIND_VALUE:
          serviceImpl.findValue((kademlia.FindValueRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.FindValueResponse>) responseObserver);
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
      return kademlia.KademliaOuterClass.getDescriptor();
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
