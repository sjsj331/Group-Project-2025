package 연습02;

import java.util.Scanner;

// 기초 JAVA 문형 학습용
class 가위바위보 {    
     
    static void 결과출력(int 승부결과, int 사람선택, int 컴선택) {
        String[] 선택 = { "가위", "바위", "보" };
        String[] 결과 = {
            "  (O_O) 비겼습니다",
            "  (^_^) 사람이 이겼습니다",
            "  (-_-) 사람이 졌습니다"
        };
        
        switch (승부결과) {
            case 0: case 1: case 2:
                System.out.print(결과[승부결과]);
                break;
            default:
                System.out.println("\t오류가 발생되었습니다.");
                return;
        }
        
        System.out.printf("(사람 = %s, 컴퓨터 = %s)%n", 선택[사람선택], 선택[컴선택]);
    }
    
    // return - 0:비김, 1:사람승, 2:컴승, -1:오류
    static int 승부판단(int 사람선택, int 컴선택) {
        if (사람선택 == 컴선택) {
            return 0;
        } else if (Math.abs(사람선택 - 컴선택) == 1) {
            return (사람선택 > 컴선택) ? 1 : 2;
        } else if (Math.abs(사람선택 - 컴선택) == 2) {
            return (사람선택 > 컴선택) ? 2 : 1;
        } else {
            return -1;
        }
    }
    
    // 0,1,2만 입력 받기
    static int 사람선택_집요하게정수012입력() {
        Scanner 키보드 = new Scanner(System.in);
        boolean 입력완료 = false;
        int 선택 = 0;
        
        while (!입력완료) {
            try { // 사용자의 입력 값을 받을때
                System.out.print("가위바위보 선택(0:가위, 1:바위, 2:보): ");
                선택 = 키보드.nextInt();
                
                if (선택 >= 0 && 선택 <= 2) {
                    입력완료 = true;
                } else {
                    System.out.println("\t잘못 입력했습니다. 0, 1, 2만 입력해야 합니다.\n");
                }
                
            } catch (Exception e) {
                System.out.println("\t잘못 입력했습니다. 정수만 입력해야 합니다.\n");
                키보드.next(); // 버퍼 비우기
            }
        }
        
        return 선택;
    }
    
    static int 컴선택() {
        return (int) (Math.random() * 3);
    }
   
    // 코인 입력 받아서 그 수만큼 판수로 사용
    static int 코인입력() {
        Scanner 코인 = new Scanner(System.in);
        System.out.print("코인을 얼마나 넣으시겠습니까?\n");
        int 코인개수 = 코인.nextInt();
        System.out.printf("입력하신 코인 개수: %d개%n", 코인개수);
        코인.nextLine();            // Scanner 닫기
        return 코인개수;      // 판수로 사용할 값을 돌려줌
    }
}

public class 주클래스2 {
    public static void main(String[] args) {
        //TODO Auto-generated method stub
        
        int 총판수 = 가위바위보.코인입력();
        int 사람선택, 컴선택, 결과;
        int 사람승 = 0, 컴승 = 0;
        System.out.printf("컴퓨터와 가위바위보 게임을 %d판 진행합니다(많이 이긴 쪽이 승!!)%n%n", 총판수);
        
        for (int 판수 = 1; 판수 <= 총판수; 판수++) {
            System.out.printf("---- [제 %d판] ----%n", 판수);
            
            사람선택 = 가위바위보.사람선택_집요하게정수012입력();
            컴선택 = 가위바위보.컴선택();
            결과 = 가위바위보.승부판단(사람선택, 컴선택);
            
            가위바위보.결과출력(결과, 사람선택, 컴선택);
            
            if (결과 == 1) 사람승++;
            else if (결과 == 2) 컴승++;
            
            System.out.printf("현재 승수(사람:컴) = %d : %d%n%n", 사람승, 컴승);
        }
        
        // 최종 승리 수 비교
        if (사람승 > 컴승) {
            System.out.printf("(^_^) 최종적으로 사람이 이겼습니다! (사람:컴 = %d:%d)%n", 사람승, 컴승);
        } else if (사람승 < 컴승) {
            System.out.printf("(-_-) 최종적으로 사람이 졌습니다. (사람:컴 = %d:%d)%n", 사람승, 컴승);
        } else {
            System.out.printf("(O_O) 최종적으로 비겼습니다. (사람:컴 = %d:%d)%n", 사람승, 컴승);
        }
    }
}