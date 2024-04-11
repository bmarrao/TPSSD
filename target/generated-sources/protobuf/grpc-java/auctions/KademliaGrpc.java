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
  public static final io.grpc.MethodDescriptor<auctions.NotifyRequest,
      com.google.protobuf.Empty> METHOD_NOTIFY =
      io.grpc.MethodDescriptor.<auctions.NotifyRequest, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "notify"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.NotifyRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<auctions.getPriceRequest,
      auctions.getPriceResponse> METHOD_GET_PRICE =
      io.grpc.MethodDescriptor.<auctions.getPriceRequest, auctions.getPriceResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "auctions.Kademlia", "getPrice"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.getPriceRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              auctions.getPriceResponse.getDefaultInstance()))
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
    public void notify(auctions.NotifyRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_NOTIFY, responseObserver);
    }

    /**
     */
    public void getPrice(auctions.getPriceRequest request,
        io.grpc.stub.StreamObserver<auctions.getPriceResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_PRICE, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_NOTIFY,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.NotifyRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_NOTIFY)))
          .addMethod(
            METHOD_GET_PRICE,
            asyncUnaryCall(
              new MethodHandlers<
                auctions.getPriceRequest,
                auctions.getPriceResponse>(
                  this, METHODID_GET_PRICE)))
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
    public void notify(auctions.NotifyRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_NOTIFY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPrice(auctions.getPriceRequest request,
        io.grpc.stub.StreamObserver<auctions.getPriceResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_PRICE, getCallOptions()), request, responseObserver);
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
    public com.google.protobuf.Empty notify(auctions.NotifyRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_NOTIFY, getCallOptions(), request);
    }

    /**
     */
    public auctions.getPriceResponse getPrice(auctions.getPriceRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_PRICE, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> notify(
        auctions.NotifyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_NOTIFY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<auctions.getPriceResponse> getPrice(
        auctions.getPriceRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_PRICE, getCallOptions()), request);
    }
  }

  private static final int METHODID_NOTIFY = 0;
  private static final int METHODID_GET_PRICE = 1;

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
        case METHODID_NOTIFY:
          serviceImpl.notify((auctions.NotifyRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_PRICE:
          serviceImpl.getPrice((auctions.getPriceRequest) request,
              (io.grpc.stub.StreamObserver<auctions.getPriceResponse>) responseObserver);
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
              .addMethod(METHOD_NOTIFY)
              .addMethod(METHOD_GET_PRICE)
              .build();
        }
      }
    }
    return result;
  }
}
