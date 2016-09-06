package de.petendi.ethereum.android.sample.contract;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;


@RunWith(JUnit4.class)
public class SimpleOwnedStorageTest extends TestCase {

    @Test
    public void testCompile() throws IOException {
        compile();
    }


    private void compile() throws IOException {
        InputStream solidityStream =  this.getClass().getResourceAsStream("/SimpleOwnedStorage.sol");
        String solidityString = IOUtils.toString(solidityStream);
        SolidityCompiler.Result result = SolidityCompiler.compile(solidityString.getBytes(), true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        CompilationResult res = CompilationResult.parse(result.output);
        CompilationResult.ContractMetadata metadata = res.contracts.get("SimpleOwnedStorage");
        System.out.println(metadata.bin);
        System.out.println(metadata.abi);

    }

}