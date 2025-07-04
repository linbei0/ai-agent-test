package com.ai.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


public class TerminalOperationToolTest {

    @Test
    public void executeTerminalCommandByWindow() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "dir";
        String result = tool.executeTerminalCommandByWindow(command);
        assertNotNull(result);
    }
}
