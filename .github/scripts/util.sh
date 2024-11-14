#!/bin/bash -e

test() {
  if [ "$2" = "$3" ]; then
    echo "OK: the $1 is $3"
  else
    echo "Fail: the $1 is $2, expected $3"
    exit 1
  fi
}

test_array() {
  local name="$1"
  local array_name="$2"
  local expected_length_id="$3"
  local expected_array_length="$4"

  # Get the length of the array (number of elements)
  local len=$(eval "echo \${#$array_name[@]}")

  # Check if the array has the expected number of elements
  if [ "$len" -ne "$expected_array_length" ]; then
    echo "Fail: the $name array has an unexpected number of elements ($len vs $expected_array_length)"
    exit 1
  fi

  # Compare the length of each string in the array
  for i in $(seq 0 $((len - 1))); do
    local val=$(eval "echo \${$array_name[$i]}")

    # Check if the string has the same expected length
    if [ ${#val} -ne "$expected_length_id" ]; then
      echo "Fail: the string at index $i in the $name array does not have the expected length of $expected_length_id: ${#val} != $expected_length_id"
      exit 1
    fi
  done

  echo "OK: all strings in the $name array have the expected length of $expected_length_id, and the array has the expected number of elements ($expected_array_length)"
}
