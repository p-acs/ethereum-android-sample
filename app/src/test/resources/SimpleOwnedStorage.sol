contract owned {
    function owned() {
        owner = msg.sender;
    }
    modifier onlyowner() {
        if (msg.sender == owner)
            _
    }
    address owner;
}

contract SimpleOwnedStorage is owned {

    string data;

    function SimpleOwnedStorage(string d) {
        data = d;
        owner = msg.sender;
    }

    function get() constant returns (string) {
		return data;
	}

	function set(string d) onlyowner{
    		data = d;
    	}

	function currentOwner() constant returns (address) {
		return owner;
	}

}