package io.quarkus.grpc.examples.hello;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import examples.Greeter;
import examples.GreeterGrpc;
import examples.HelloReply;
import examples.HelloRequest;
import io.opentelemetry.api.trace.Span;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloWorldEndpoint {

    @GrpcClient("hello")
    GreeterGrpc.GreeterBlockingStub blockingHelloService;

    @GrpcClient("hello")
    Greeter helloService;

    @GET
    @Path("/blocking/{name}")
    public String helloBlocking(@PathParam("name") String name) {
        HelloReply reply = blockingHelloService.sayHello(HelloRequest.newBuilder().setName(name).build());
        return generateResponse(reply);

    }

    @GET
    @Path("/mutiny/{name}")
    public Uni<String> helloMutiny(@PathParam("name") String name) {
        return helloService.sayHello(HelloRequest.newBuilder().setName(name).build())
                .onItem().transform((reply) -> {
                    System.out.println("helloMutiny " + Span.current().getSpanContext().getTraceId());

                    return generateResponse(reply);
                });
    }

    public String generateResponse(HelloReply reply) {
        return String.format("%s! HelloWorldService has been called %d number of times.", reply.getMessage(), reply.getCount());
    }
}
