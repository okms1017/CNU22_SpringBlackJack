package kr.ac.cnu.web.controller.api;

import kr.ac.cnu.web.exceptions.NoLoginException;
import kr.ac.cnu.web.exceptions.NoUserException;
import kr.ac.cnu.web.games.blackjack.GameRoom;
import kr.ac.cnu.web.games.blackjack.Player;
import kr.ac.cnu.web.model.User;
import kr.ac.cnu.web.repository.UserRepository;
import kr.ac.cnu.web.service.BlackjackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by rokim on 2018. 5. 21..
 */
@RestController
@RequestMapping("/api/black-jack")
@CrossOrigin
public class BlackApiController {
    @Autowired
    private BlackjackService blackjackService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User login(@RequestBody String name) {
        return userRepository.findById(name).orElseThrow(() -> new NoUserException());
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User signup(@RequestBody String name) {
        //TODO check already used name
        Optional<User> userOptimal = userRepository.findById(name);
        if(userOptimal.isPresent()){
            throw new RuntimeException();
        }

        //TODO new user
        User user = new User(name, 50000);

        //TODO save in repository
        return userRepository.save(user);
    }

    @PostMapping("/rooms")
    public GameRoom createRoom(@RequestHeader("name") String name) {
        User user = this.getUserFromSession(name);

        return blackjackService.createGameRoom(user);
    }

    @PostMapping(value = "/rooms/{roomId}/bet", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GameRoom bet(@RequestHeader("name") String name, @PathVariable String roomId, @RequestBody long betMoney) {
        User user = this.getUserFromSession(name);
        return blackjackService.bet(roomId, user, betMoney);
    }

    @PostMapping("/rooms/{roomId}/hit")
    public GameRoom hit(@RequestHeader("name") String name, @PathVariable String roomId) {
        User user = this.getUserFromSession(name);
        Player player = blackjackService.getGameRoom(roomId).getPlayerList().get(name);
        GameRoom hitService = blackjackService.hit(roomId, user);
        user.setAccount(player.getBalance());
        userRepository.save(user);
        return hitService;
    }

    @PostMapping("/rooms/{roomId}/stand")
    public GameRoom stand(@RequestHeader("name") String name, @PathVariable String roomId) {
        User user = this.getUserFromSession(name);
        Player player = blackjackService.getGameRoom(roomId).getPlayerList().get(name);
        GameRoom standService = blackjackService.stand(roomId, user);
        user.setAccount(player.getBalance());
        userRepository.save(user);
        return standService;
    }

    @PostMapping("/rooms/{roomId}/doubleDown")
    public GameRoom doubleDown(@RequestHeader("name") String name, @PathVariable String roomId, @RequestBody long betMoney) {
        User user = this.getUserFromSession(name);
        Player player = blackjackService.getGameRoom(roomId).getPlayerList().get(name);
        GameRoom doubleService = blackjackService.doubleDown(roomId, user, betMoney);
        user.setAccount(player.getBalance());
        userRepository.save(user);
        return doubleService;
    }

    @GetMapping("/rooms/{roomId}")
    public GameRoom getGameRoomData(@PathVariable String roomId) {
        return blackjackService.getGameRoom(roomId);
    }

    @PostMapping("/rooms/{roomId}/rank")
    public List rank(@RequestHeader("name") String name, @PathVariable String roomId) {
        List<User> userList = userRepository.findAll();
        List<User> rankList = decendingSort(userList);
        return userList;
    }

    private User getUserFromSession(String name) {
        return userRepository.findById(name).orElseThrow(() -> new NoLoginException());
    }

    //랭킹 서비스를 위한 내림차순 정령
    private List<User> decendingSort(List<User> userList){
        Collections.sort(userList, new Decending());
        return userList;
    }

    //내림차순을 위한 내부 클래스 구현
    class Decending implements Comparator<User>{

        @Override
        public int compare(User o1, User o2) {
            Long value1 = o1.getAccount();
            Long value2 = o2.getAccount();
            return value2.compareTo(value1);
        }
    }
}
