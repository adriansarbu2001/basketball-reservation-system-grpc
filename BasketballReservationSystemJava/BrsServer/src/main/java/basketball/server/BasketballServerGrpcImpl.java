package basketball.server;

import basketball.model.User;
import basketball.model.Match;
import basketball.model.Ticket;
import basketball.model.validator.ValidatorException;
import basketball.network.protobuffprotocol.BasketballProtobufs.BasketballResponse;
import basketball.network.protobuffprotocol.BasketballProtobufs.BasketballRequest;
import basketball.network.protobuffprotocol.BasketballProtobufs.UserDto;
import basketball.network.protobuffprotocol.BasketballProtobufs.MatchDto;
import basketball.network.protobuffprotocol.BasketballProtobufs.TicketDto;
import basketball.network.protobuffprotocol.BasketballServiceGrpc;
import basketball.persistence.IUserRepository;
import basketball.persistence.IMatchRepository;
import basketball.persistence.ITicketRepository;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BasketballServerGrpcImpl extends BasketballServiceGrpc.BasketballServiceImplBase {

    private final IUserRepository accountRepository;
    private final IMatchRepository matchRepository;
    private final ITicketRepository ticketRepository;
    private final Set<StreamObserver<BasketballResponse>> observers;
    private final Set<Long> loggedUsers;

    public BasketballServerGrpcImpl(IUserRepository accountRepository, IMatchRepository matchRepository, ITicketRepository ticketRepository) {
        this.accountRepository = accountRepository;
        this.matchRepository = matchRepository;
        this.ticketRepository = ticketRepository;
        observers = new LinkedHashSet<>();
        loggedUsers = new LinkedHashSet<>();
    }
    @Override
    public StreamObserver<BasketballRequest> subscribe(StreamObserver<BasketballResponse> responseObserver) {
        observers.add(responseObserver);

        return new StreamObserver<>() {
            @Override
            public void onNext(BasketballRequest value) {
                for (StreamObserver<BasketballResponse> observer : observers) {
                    observer.onNext(BasketballResponse.newBuilder().setType(BasketballResponse.Type.TICKET_SOLD).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                observers.remove(responseObserver);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                observers.remove(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void login(BasketballRequest request, StreamObserver<BasketballResponse> responseObserver) {
        System.out.println("Am primit request " + request.getType());
        UserDto userDto = request.getUserDto();
        User user = accountRepository.findBy(userDto.getUsername(), userDto.getPassword());
        BasketballResponse response;
        if (user != null) {
            if (loggedUsers.contains(user.getId())) {
                response = BasketballResponse.newBuilder()
                        .setType(BasketballResponse.Type.ERROR)
                        .setError("User already logged in.")
                        .build();
            } else {
                response = BasketballResponse.newBuilder()
                        .setType(BasketballResponse.Type.OK)
                        .setUserDto(DtoUtils.fromUser(user))
                        .build();
                loggedUsers.add(user.getId());
            }

        } else {
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.ERROR)
                    .setError("Authentication failed.")
                    .build();

        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void logout(BasketballRequest request, StreamObserver<BasketballResponse> responseObserver) {
        System.out.println("Am primit request " + request.getType());
        UserDto userDto = request.getUserDto();
        BasketballResponse response;
        if (loggedUsers.remove(userDto.getId())) {
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.OK)
                    .build();
        } else {
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.ERROR)
                    .setError("User " + userDto.getId() + " is not logged in.")
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void findAll(BasketballRequest request, StreamObserver<BasketballResponse> responseObserver) {
        System.out.println("Am primit request " + request.getType());
        List<MatchDto> all = new ArrayList<>();
        for (Match m : matchRepository.findAll()) {
            all.add(DtoUtils.fromMatch(m));
        }
        BasketballResponse response = BasketballResponse.newBuilder()
                .setType(BasketballResponse.Type.OK)
                .addAllMatchDtos(all)
                .build();
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void saveTicket(BasketballRequest request, StreamObserver<BasketballResponse> responseObserver) {
        System.out.println("Am primit request " + request.getType());
        TicketDto t = request.getTicketDto();
        BasketballResponse response;
        Match match = matchRepository.findOne(t.getMatchId());
        if (match.getNoAvailableSeats() - t.getNoSeats() < 0) {
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.ERROR)
                    .setError("Not enough seats available!")
                    .build();
            responseObserver.onNext(response);
            System.out.println("Am trimis raspuns " + response.getType() + "\n");
            responseObserver.onCompleted();
            return;
        }
        Ticket ticket = DtoUtils.fromTicketDto(t);
        try {
            ticketRepository.save(ticket);
            match.setNoAvailableSeats(match.getNoAvailableSeats() - ticket.getNoSeats());
            matchRepository.update(match);
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.OK)
                    .build();
        } catch (ValidatorException e) {
            response = BasketballResponse.newBuilder()
                    .setType(BasketballResponse.Type.ERROR)
                    .setError(e.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void availableMatchesDescending(BasketballRequest request, StreamObserver<BasketballResponse> responseObserver) {
        System.out.println("Am primit request " + request.getType());
        List<MatchDto> all = new ArrayList<>();
        for (Match m : matchRepository.availableMatchesDescending()) {
            all.add(DtoUtils.fromMatch(m));
        }
        BasketballResponse response = BasketballResponse.newBuilder()
                .setType(BasketballResponse.Type.OK)
                .addAllMatchDtos(all)
                .build();
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }
}
