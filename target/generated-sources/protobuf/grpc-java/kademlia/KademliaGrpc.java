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
  public static final io.grpc.MethodDescriptor<kademlia.FindAuctionRequest,
      kademlia.FindAuctionResponse> METHOD_FIND_AUCTION =
      io.grpc.MethodDescriptor.<kademlia.FindAuctionRequest, kademlia.FindAuctionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "kademlia.Kademlia", "findAuction"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindAuctionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              kademlia.FindAuctionResponse.getDefaultInstance()))
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
    public void ping(kademlia.PingRequest request,
        io.grpc.stub.StreamObserver<kademlia.PingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PING, responseObserver);
    }

    /**
     * <pre>
     *rpc storeTransaction(StoreTransactionRequest) returns (StoreTransactionResponse);
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_NODE, responseObserver);
    }

    /**
     */
    public void findAuction(kademlia.FindAuctionRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindAuctionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FIND_AUCTION, responseObserver);
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
            METHOD_FIND_NODE,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.FindNodeRequest,
                kademlia.FindNodeResponse>(
                  this, METHODID_FIND_NODE)))
          .addMethod(
            METHOD_FIND_AUCTION,
            asyncUnaryCall(
              new MethodHandlers<
                kademlia.FindAuctionRequest,
                kademlia.FindAuctionResponse>(
                  this, METHODID_FIND_AUCTION)))
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
    public void ping(kademlia.PingRequest request,
        io.grpc.stub.StreamObserver<kademlia.PingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *rpc storeTransaction(StoreTransactionRequest) returns (StoreTransactionResponse);
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public void findNode(kademlia.FindNodeRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findAuction(kademlia.FindAuctionRequest request,
        io.grpc.stub.StreamObserver<kademlia.FindAuctionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FIND_AUCTION, getCallOptions()), request, responseObserver);
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
    public kademlia.PingResponse ping(kademlia.PingRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PING, getCallOptions(), request);
    }

    /**
     * <pre>
     *rpc storeTransaction(StoreTransactionRequest) returns (StoreTransactionResponse);
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public kademlia.FindNodeResponse findNode(kademlia.FindNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_NODE, getCallOptions(), request);
    }

    /**
     */
    public kademlia.FindAuctionResponse findAuction(kademlia.FindAuctionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FIND_AUCTION, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<kademlia.PingResponse> ping(
        kademlia.PingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request);
    }

    /**
     * <pre>
     *rpc storeTransaction(StoreTransactionRequest) returns (StoreTransactionResponse);
     * 160-bit key as an argument, returns (IP address, UDP port, Node ID) for each k closest nodes to target id
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.FindNodeResponse> findNode(
        kademlia.FindNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_NODE, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<kademlia.FindAuctionResponse> findAuction(
        kademlia.FindAuctionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FIND_AUCTION, getCallOptions()), request);
    }
  }

  private static final int METHODID_PING = 0;
  private static final int METHODID_FIND_NODE = 1;
  private static final int METHODID_FIND_AUCTION = 2;

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
        case METHODID_FIND_NODE:
          serviceImpl.findNode((kademlia.FindNodeRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.FindNodeResponse>) responseObserver);
          break;
        case METHODID_FIND_AUCTION:
          serviceImpl.findAuction((kademlia.FindAuctionRequest) request,
              (io.grpc.stub.StreamObserver<kademlia.FindAuctionResponse>) responseObserver);
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
              .addMethod(METHOD_FIND_NODE)
              .addMethod(METHOD_FIND_AUCTION)
              .build();
        }
      }
    }
    return result;
  }
}
