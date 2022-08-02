package java.dk.dtu.compute.se.pisd.roborally.controller;
import dk.dtu.compute.se.pisd.roborally.controller.SaveAndLoad;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeAndDeserialize;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializeTest {

    @Test
    void Serializes_Deserializes_DefaultBoard() {
        try {
            Board startBoard = SaveAndLoad.newBoard(3, "SpringCramp");
            String jsonResult1 = SerializeAndDeserialize.serialize(startBoard);

            Board board1 = SerializeAndDeserialize.deserialize(jsonResult1, false);
            String jsonResult2 = SerializeAndDeserialize.serialize(board1);

            Assertions.assertEquals(jsonResult1, jsonResult2);
        } catch (BoardDoesNotExistException e) {
            assert true;
        }
    }


    @Test
    void Serializes_Deserializes_SavedBoard() {
        try {
            Board startBoard = SaveAndLoad.loadBoardGame("Save Testing");
            String jsonResult1 = SerializeAndDeserialize.serialize(startBoard);

            Board board1 = SerializeAndDeserialize.deserialize(jsonResult1, true);
            String jsonResult2 = SerializeAndDeserialize.serialize(board1);

            Assertions.assertEquals(jsonResult1, jsonResult2);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


